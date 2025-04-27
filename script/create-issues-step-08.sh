#!/bin/bash

FILE="step08-issues.yml"
REPO="thedev-junyoung/hanghae-plus-4wd"

for i in $(seq 0 $(($(yq '.issues | length' "$FILE") - 1))); do
  title=$(yq ".issues[$i].title" "$FILE")
  body=$(yq ".issues[$i].body" "$FILE")
  labels=$(yq ".issues[$i].labels | join(\",\")" "$FILE")

  echo "📌 Creating issue: $title"
  gh issue create --repo "$REPO" --title "$title" --body "$body" --label "$labels"
done
