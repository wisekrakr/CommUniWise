![Master](https://github.com/ipphone/core/workflows/Master/badge.svg)
<img src="https://img.shields.io/badge/Java-build%20with%20Java-blue"/>
![version](https://img.shields.io/badge/version-0.5.9-blue)

    
                                  CommUniWise: java communication app
                                 http://github.com/wisekrakr/CommUniWise



<a href="https://twitter.com/intent/follow?screen_name=wisekrakr">
        <img src="https://img.shields.io/twitter/follow/wisekrakr?style=social&logo=twitter"
            alt="follow on Twitter"></a>
            
       

CommUniWise provides an object-oriented Java API for embedding
two-way audio (video and message(WIP)). This is a pure client-side solution and requires zero 
server-side logic on your part.

CommUniWise also uses my own build library to create Swing JFrames, but with the aesthetics of a JavaFX Pane.

CommUniWise requires an already setup server. Register or login with username and password.

#### WORKS IN PROGRESS
- Video and messaging (20% finished)
- Choosing input and audio via GUI, instead of Commons-cli (50% finished)
- Recording of a call (90% finished)
- Playing audio remotely is finished, but audio still seems choppy

#### SPECIFICATION

CommUniWise is a software phone (softphone) compatible with the
following specifications:
 - RFC 3261 (SIP),
 - RFC 4566 (SDP),
 - RFC 3550 (RTP),
 - RFC 3551 (RTP Audio/Video profile),
 - RFC 2617 (Digest Authentication),
 - ITU-T G.722 (PCMU, PCMA)

#### PREREQUISITES

This software has been developed using Oracle Java Development Kit
version 7.

#### MAVEN DEPENDENCIES

These are the dependencies used in the project:
 - Commons-cli 
 - Jain-sip-api 
 - Jain-sip-ri 
 - Jain-sdp 
 - Jain-sip-sdp 
 - Jain-sip-tck 
 - Log4j 
 - Commons-lang3 
 - org.ocpsoft.prettytime
 - org.beryx text.io
 - JUnit


#### USAGE

In program arguments use the following:
- **-ip <ip address>:** _Your IP address_
- **-i <audio input device>:** _Name of your audio input device to be used_
- **-o <audio output device>:** _Name of your audio output device to be used_

> **For example**, :
> - **-ip** `127.0.0.1`
> - **-i** `Microphone (Best Mics V2)`
> - **-o** `Speakers (Big Boi Speakers)`
>

SIP account configuration settings:
- **Username:** _name used to register on the domain_
- **Domain:** _domain name_ (like: asterisk.<whatever>)
- **Password:** _sip account password_
- **Realm:** _*_ (done automatically)
- **Proxy Address:** _*_ (done automatically)
- **SIP Registrar:** asterisk server address (server IP or DNS name)


> **For example**, if you have SIP account `666@asterisk.local` with password `1101101`, configuration settings you would use:
> - **Display Name:** `666@asterisk.local`
> - **Username:** `wisekrakr`
> - **Password:** `1101101`
> - **Realm:** `asterisk`
> - **SIP Registrar:** `asterisk.local`
>

## AUTHOR

David Buendia Cosano davidiscodinghere@gmail.com

## Extra trivia

This app was build, listening exclusively to 70's African Funk, Blues and Jazz.
https://open.spotify.com/playlist/5Jwbf4n4eFm3V1oDEvuU1U?si=4TZ1NvJ_S7ath1A_W8lnLg
