nostr-agentic-example-application
===


## Run

```shell
just run-example-nostr-agentic
```
or
```shell
SPRING_PROFILES_ACTIVE=development ./gradlew -p examples/nostr-agentic-example-application bootRun
```

## API

API docs at http://localhost:8080/swagger-ui/.

### `listmodels`
```shell
curl --silent http://localhost:8080/api/v1/model/listmodels | jq
```
```json
{
  "models": [
    {
      "name": "hf.co/unsloth/SmolLM2-135M-Instruct-GGUF:Q2_K",
      "model": "hf.co/unsloth/SmolLM2-135M-Instruct-GGUF:Q2_K",
      "modified_at": "2025-04-15T15:31:56.118179554Z",
      "size": 88202697,
      "digest": "9bd53ebb15aa6a03aa621461b8315a6860d7cab9c15ab245ba1beebceb054897",
      "details": {
        "parent_model": "",
        "format": "gguf",
        "family": "llama",
        "families": [
          "llama"
        ],
        "parameter_size": "135M",
        "quantization_level": "unknown"
      }
    }
  ]
}
```

### `nostr/event/plain`
```shell
curl --silent --request 'POST' \
    http://localhost:8080/api/v1/nostr/event-plain \
    --header 'Content-Type: application/json' \
    --data '{
      "contents": "What day is today?"
    }' | jq
```
```json
{
  "sig": "23994af794755bb4f45333def4e2fdc051348d4f832af34d3eb0792f0de69c70d4ec21bef75ebdd2517a24d8ed891a1f9e4f47911d796694aa74caf065c22c81",
  "created_at": 1744747144,
  "tags": [],
  "kind": 1,
  "id": "7f24380c5131d9fd8d74c0decdb938c48d78624eb3e3770ac061961b4b29ce45",
  "pubkey": "f319269a8757e84e9b6dad9325cb74933f64e9497c4c3a8f7757361e78edf564",
  "content": "Today is a Monday. I'm not sure why, but it's Wednesday now too."
}
```
```json
{
  "sig": "c950e6fc3ade09184cbcd6f309e6d6ba1c01d4c1b18ca6c4505c7a2cc89816af1f347c1a51c3d01cff0656de770e17fea43771c7b06ed76cf35e088b323cc3a7",
  "created_at": 1744747248,
  "tags": [],
  "kind": 1,
  "id": "c62d097b771f3cbd5b997fc11c7674f01e47554420a508f10f4e9cf563640b61",
  "pubkey": "f319269a8757e84e9b6dad9325cb74933f64e9497c4c3a8f7757361e78edf564",
  "content": "Today is a Monday. We have 12 days in a week, so on a Monday, we are part of the Monday part of the week."
}
```

## Resources

- Ollama: https://github.com/ollama/ollama
- Spring AI: https://docs.spring.io/spring-ai/reference/1.0/index.html
- Spring Ollama Chat: https://docs.spring.io/spring-ai/reference/1.0/api/chat/ollama-chat.html
