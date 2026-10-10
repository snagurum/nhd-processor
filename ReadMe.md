
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


