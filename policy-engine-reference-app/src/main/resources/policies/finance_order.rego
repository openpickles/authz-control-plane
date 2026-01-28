package policies.finance_order

default allow = false

allow {
    input.amount < 1000
}
