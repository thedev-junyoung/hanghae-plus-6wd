#!/bin/bash

REPO="thedev-junyoung/hanghae-plus-4wd"

labels=("infra" "test" "balance" "product" "order" "payment" "coupon" "productstatistics" "orderexport")

for label in "${labels[@]}"; do
  echo "🔖 Creating label: $label"
  gh label create "$label" --repo "$REPO" --color "ededed" --force
done

