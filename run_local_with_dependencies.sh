#!/bin/bash

sm2 --start MONGO EMAIL HMRC_EMAIL_RENDERER MAILGUN_STUB THIRD_PARTY_APPLICATION

sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
