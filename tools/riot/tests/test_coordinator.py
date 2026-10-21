"""No external API key is needed for this regression suite."""
import json
import os
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from coordinator import summary_from_records, run_live
from rift_prism import example_record, gold10_snapshot, compute_report

class RiotCoordinatorTests(unittest.TestCase):
    def test_synthetic_demo_shows_masked_role_gap(self):
        sample = example_record()
        result = gold10_snapshot(sample["match"], sample["timeline"])
        self.assertIsNotNone(result)
        self.assertEqual(result["team_gold_diff_at_10"], 0)
        self.assertEqual(result["role_gold_diffs_at_10"]["TOP"], 1900)
        self.assertTrue(result["masked_lane_gap"])

    def test_exports_only_aggregates(self):
        sample = example_record()
        sample["match"]["metadata"]["matchId"] = "PRIVATE_MATCH_ID"
        sample["match"]["info"]["participants"][0]["puuid"] = "PRIVATE_PUUID"
        summary = summary_from_records([sample])
        serialized = json.dumps(summary)
        self.assertNotIn("PRIVATE_MATCH_ID", serialized)
        self.assertNotIn("PRIVATE_PUUID", serialized)
        self.assertNotIn("matches",summary)
        self.assertFalse(summary["contains_raw_player_identifiers"])

    def test_duplicate_records_ignored(self):
        sample = example_record()
        self.assertEqual(compute_report([sample,sample])["complete_ranked_solo_samples"], 1)

    def test_missing_key_rejected(self):
        with patch.dict(os.environ,{"RIOT_API_KEY":""}):
            with self.assertRaisesRegex(ValueError,"Missing RIOT_API_KEY"):
                run_live("Account","TEST","europe",1)

    def test_mocked_live_collects_without_raw_export(self):
        sample = example_record()
        sample["match"]["metadata"]["matchId"] = "SECRET_TEST_ID"
        def fake_collect(name,tag,region,count,token,out):
            out.write_text(json.dumps(sample)+"\n", encoding="utf-8")
            return {"newly_saved":1}
        with patch.dict(os.environ,{"RIOT_API_KEY":"FAKE_TEST_KEY"}):
            with patch("coordinator.collect",side_effect=fake_collect):
                result=run_live("Account","TEST","europe",1)
        self.assertEqual(result["complete_ranked_solo_samples"],1)
        self.assertNotIn("SECRET_TEST_ID",json.dumps(result))
        self.assertNotIn("FAKE_TEST_KEY",json.dumps(result))

    def test_unranked_data_excluded(self):
        sample=example_record()
        sample["match"]["info"]["queueId"]=400
        self.assertEqual(summary_from_records([sample])["complete_ranked_solo_samples"],0)

if __name__ == "__main__":
    unittest.main()
