#!/bin/bash
awslocal s3 mb s3://cotubify-ebooks


# deixa os objetos legíveis sem assinatura, para o link do e-mail funcionar no navegador
awslocal s3api put-bucket-policy --bucket cotubify-ebooks --policy '{
 "Version": "2012-10-17",
 "Statement": [{
   "Effect": "Allow",
   "Principal": "*",
   "Action": "s3:GetObject",
   "Resource": "arn:aws:s3:::cotubify-ebooks/*"
 }]
}'
