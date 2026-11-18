
# Rule Engine testcase.

## create rule testcase

```
curl -X POST http://localhost:8080/re/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Shipping Policy",
    "description": "Calculates shipping rates based on order value",
    "ruleItems": [
      {
        "condition": "cart.total >= 100.00",
        "thenValue": "shipping.cost = 0.00",
        "elseValue": "shipping.cost = 9.99"
      },
      {
        "condition": "user.isPremium == true",
        "thenValue": "shipping.cost = 0.00",
        "elseValue": null
      }
    ]
  }'
```


## create rule all rules testcase
curl -X GET http://localhost:8080/re/api/rules \
  -H "Accept: application/json"




## Stocks data service

Processor runs on `http://localhost:8081/re` and owns rules, ticker-list configuration and job runners in schema `re`. NSE persistence and the public stock-stats API live in sibling project `../nhd-stocksdata`, running on `http://localhost:8082/stocks`. Start stocksdata before running jobs.

Set `STOCKSDATA_URL` to override the service URL. `processor.stocksdata-write-batch-size` controls HTTP write batches (default 2000). Reads and writes use HTTP; stock models in processor are plain rule-context/wire objects, retaining their class names for stored MVEL rules. HTTP failures propagate and leave the persistence buffer available for retry; writes use existing upserts.

See `../nhd-stocksdata/ReadMe.md` for migration sequencing and the API contract. The stock-stats endpoint moved to `http://localhost:8082/stocks/api/v1/stockStats`.
