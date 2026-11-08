// Atlas I/O API v0.1: minimal Cloudflare Worker with D1-backed observations.
// Server-to-server only. Never expose ATLAS_IO_TOKEN in client-side code.
const json = (data, status = 200, headers = {}) =>
  new Response(JSON.stringify(data), {
    status,
    headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store", ...headers }
  });
const fail = (status, code, message) => json({ error: { code, message } }, status);
const MAX_BYTES = 65536;

function isAuthorized(request, env) {
  const header = request.headers.get("authorization") || "";
  const token = env.ATLAS_IO_TOKEN;
  return Boolean(token && header.startsWith("Bearer ") &&
    header.slice(7) === token);
}
function validField(value) {
  return typeof value === "string" && /^[a-zA-Z0-9_.:-]{1,80}$/.test(value);
}
async function readLimitedJson(request) {
  const contentType = request.headers.get("content-type") || "";
  if (!/^application\/json(?:\s*;|\s*$)/i.test(contentType))
    return { error: fail(415, "UNSUPPORTED_MEDIA_TYPE", "Use application/json") };
  const length = Number(request.headers.get("content-length"));
  if (Number.isFinite(length) && length > MAX_BYTES)
    return { error: fail(413, "TOO_LARGE", "Maximum request size is 64 KiB") };
  if (!request.body) return { error: fail(400, "INVALID_JSON", "Missing JSON body") };
  const reader = request.body.getReader();
  let size = 0;
  const chunks = [];
  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      size += value.byteLength;
      if (size > MAX_BYTES) {
        await reader.cancel();
        return { error: fail(413, "TOO_LARGE", "Maximum request size is 64 KiB") };
      }
      chunks.push(value);
    }
    const bytes = new Uint8Array(size);
    let offset = 0;
    for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.length; }
    return { data: JSON.parse(new TextDecoder("utf-8", { fatal: true }).decode(bytes)) };
  } catch {
    return { error: fail(400, "INVALID_JSON", "Malformed JSON body") };
  }
}
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "GET" && url.pathname === "/health")
      return json({ service: "atlas-io", status: "ok", version: "0.1.0" });
    if (!env.DB) return fail(503, "NOT_CONFIGURED", "Database binding missing");
    if (!isAuthorized(request, env)) return fail(401, "UNAUTHORIZED", "Valid bearer token required");
    try {
      if (url.pathname === "/v1/observations" && request.method === "POST") {
        const incoming = await readLimitedJson(request);
        if (incoming.error) return incoming.error;
        const item = incoming.data;
        if (!item || Array.isArray(item) || typeof item !== "object" ||
            !validField(item.source) || !validField(item.kind) ||
            !item.payload || Array.isArray(item.payload) || typeof item.payload !== "object")
          return fail(422, "INVALID_OBSERVATION", "source, kind and object payload are required");
        const id = crypto.randomUUID();
        const payload = JSON.stringify(item.payload);
        await env.DB.prepare("INSERT INTO observations (id, source, kind, payload) VALUES (?, ?, ?, ?)")
          .bind(id, item.source, item.kind, payload).run();
        return json({ id, source: item.source, kind: item.kind }, 201);
      }
      if (url.pathname === "/v1/observations" && request.method === "GET") {
        const raw = url.searchParams.get("limit") ?? "20";
        if (!/^\d{1,3}$/.test(raw) || Number(raw) < 1 || Number(raw) > 100)
          return fail(400, "INVALID_LIMIT", "limit must be between 1 and 100");
        const kind = url.searchParams.get("kind");
        if (kind !== null && !validField(kind))
          return fail(400, "INVALID_KIND", "Invalid kind");
        const stmt = kind
          ? env.DB.prepare("SELECT id, source, kind, payload, created_at FROM observations WHERE kind = ? ORDER BY created_at DESC, rowid DESC LIMIT ?").bind(kind, Number(raw))
          : env.DB.prepare("SELECT id, source, kind, payload, created_at FROM observations ORDER BY created_at DESC, rowid DESC LIMIT ?").bind(Number(raw));
        const rows = (await stmt.all()).results ?? [];
        return json({ items: rows.map(row => ({ ...row, payload: JSON.parse(row.payload) })) });
      }
      const match = /^\/v1\/observations\/([0-9a-f-]{36})$/.exec(url.pathname);
      if (match && request.method === "GET") {
        const row = await env.DB.prepare("SELECT id, source, kind, payload, created_at FROM observations WHERE id = ?")
          .bind(match[1]).first();
        if (!row) return fail(404, "NOT_FOUND", "Observation not found");
        return json({ ...row, payload: JSON.parse(row.payload) });
      }
      return fail(404, "NOT_FOUND", "Route not found");
    } catch {
      return fail(500, "INTERNAL_ERROR", "Unexpected server error");
    }
  }
};
