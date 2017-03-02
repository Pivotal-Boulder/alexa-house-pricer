## House Pricer

A simple alexa skill that checks on the current price of your house. It
uses the free zillow API in order to get a Zestimate for a given address
and zip code.


## Setup

No database is needed for this. The only thing that is needed to run
the project on AWS Lambda or another service is an environment variable.

ZWS_ID needs to be set to the Zillow API key that was given for your
account. Go to zillow.com/api to sign up and get this key for your own
usage.

## Deploying

Use the fat jar built to upload to the AWS Lambda service. This
lambda instance will then need to be targeted by the Amazon
Developer console.

## Important Links

NOTE: These may become outdated quickly, as the Alexa system is very
new.

For Viewing Lambda Logs - https://console.aws.amazon.com/cloudwatch/home?region=us-east-1
For Deploying - https://console.aws.amazon.com/lambda/home?region=us-east-1
For Configuring/Publishing Alexa App - https://developer.amazon.com/edw/home.html
