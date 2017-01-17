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
