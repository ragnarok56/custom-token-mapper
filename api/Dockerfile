FROM ghcr.io/astral-sh/uv:alpine

WORKDIR /code

COPY pyproject.toml uv.lock /code/

RUN uv sync --locked

COPY app /code/app

EXPOSE 80

CMD ["uv", "run", "fastapi", "run", "app/main.py", "--port", "80"]