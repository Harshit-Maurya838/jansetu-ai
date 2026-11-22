from fastapi.testclient import TestClient

from app import main

client = TestClient(main.app)


def test_health_is_public():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "UP"


def test_model_endpoints_need_api_key(monkeypatch):
    monkeypatch.setattr(main, "API_KEY", "secret")
    body = {
        "child_id": "c1",
        "history": [
            {
                "measured_on": "2026-01-01",
                "age_days": 400,
                "sex": "FEMALE",
                "weight_kg": 8.0,
                "height_cm": 74.0,
                "recumbent": True,
            }
        ],
    }
    assert client.post("/v1/malnutrition/predict", json=body).status_code == 401
    assert client.post("/v1/malnutrition/predict", json=body, headers={"X-API-Key": "secret"}).status_code == 501


def test_missing_server_key_rejects_everything(monkeypatch):
    monkeypatch.setattr(main, "API_KEY", "")
    assert client.post("/v1/growth/zscores", json={}, headers={"X-API-Key": ""}).status_code == 401
