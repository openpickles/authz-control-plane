package policies.finance_invoice

default allow = false

allow {
    input.amount < 5000
    input.approved_by != null
}
