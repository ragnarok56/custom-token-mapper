from fastapi import FastAPI

app = FastAPI()

@app.get("/user/{user_id}/sources")
def read_item(user_id: str):
    return {"sources": ['source_a', 'source_b']}