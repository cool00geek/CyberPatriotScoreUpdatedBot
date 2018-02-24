# CyberPatriotScoreUpdatedBot

A bot designed to send an email when the official scores for the CyberPatriot cyber defense competition has been released.

This is currently designed to send an email using my personal library which can be overridden (the library is not included due to the contents containing personal information).

## Features
- Check the website using ChromeDriver every 3 minutes
- Send an email when it has been updated
- Send an email with the score link if it has been detected
- Open the excel document with Apache POI
- Search for the advancement column and send another email if our team (0247) has advanced.

This is currently hardcoded for team 10-0247 and the semi-final round.
