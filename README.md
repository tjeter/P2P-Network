# P2P-Network
This is the setup for a Peer-to-Peer Network similar to BitTorrent. BitTorrent is a popular Peer-to-Peer protocol for file distribution. Among its interesting features, we are asked to implement the choking/unchoking mechanism which is one of the most important features of BitTorrent.

Working:
--------
All requirements for the project are satisified. There is a script to start up all peer processes on the different machines and make the TCP connections. Pieces are exchanged as described in the protocol. The peers keep running after receiving the file until all peers have the full file. All actions happening on each peer while the program is running are logged in their own log files and debug log files. The services stop upon completion.

Issues:
-------
On occasion, one of the peers loses connection to the rest, throwing SocketException: Connection Reset when files are sent to it. It is unclear what causes this issue. It is unclear if it’s the code or if the network is unstable. If this error occurs, that peer will not be able to obtain all the pieces, and the program will not terminate normally. It is set to terminate after 7.5 minutes regardless of if the file transfer is complete or not as a fail-safe, so it will not run forever.

Video Demonstration:
--------------------
https://uflorida-my.sharepoint.com/:v:/g/personal/rangerchenore_ufl_edu/EZUodMI50K5ApDIKDV_waW0BH-_yRhSukzzmbCljjjrL_w?e=QFt1hM


(Change [insert_your_own_path] to the path to files on your own machine and [username] to your GatorLink username)

Make sure the public key (of the machine you run this script from) is in the authorized keys file of all of the remotes

Placing all class files in root (~) makes things easier

Compiling on Windows:
---------------------
javac -classpath '.;[insert_your_own_path]\jsch-0.1.54.jar;' '[insert_your_own_path]\StartRemotePeers.java'

Run StartRemotePeers:
---------------------
java -classpath ".:jsch-0.1.54.jar:" StartRemotePeers

Securely Re-Copy Class Files:
-----------------------------
scp '[insert_your_own_path]\StartRemotePeers.class' '[insert_your_own_path]\StartRemotePeers$1.class' '[insert_your_own_path]\StartRemotePeers$PeerInfo.class' [username]@lin114-01.cise.ufl.edu:~

=================================================================================================================================================================================

For remote Linux machine (Thunder)

Change Directory to Source (src):
---------------------------------
cd '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/src'

Generate Source List:
---------------------
find . -name "*.java" > sources.txt

Compile With .jar File:
-----------------------
javac -d '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin' -cp '.:/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/jsch-0.1.54.jar' @sources.txt

Securely Copy the Class Files:
------------------------------
scp /cise/homes/[username]/CNT5106C_BitTorrent/P2P\ File\ Share/bin/*.class [username]@lin114-01.cise.ufl.edu:~

Securely Copy Configuration File and File to be Shared:
-------------------------------------------------------
scp -r '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/config' [username]@lin114-01.cise.ufl.edu:~ 
scp -r '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/FileToShare' [username]@lin114-01.cise.ufl.edu:~


Altogether in One Line:
-----------------------
cd '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/src' && find . -name "*.java" > sources.txt && javac -d '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin' -cp '.:/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/jsch-0.1.54.jar' @sources.txt && scp /cise/homes/[username]/CNT5106C_BitTorrent/P2P\ File\ Share/bin/*.class [username]@lin114-01.cise.ufl.edu:~ && scp -r '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/config' [username]@lin114-01.cise.ufl.edu:~  && scp -r '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/FileToShare' [username]@lin114-01.cise.ufl.edu:~

Compile Everything Without Using Secure Copying (scp):
------------------------------------------------------
cd '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/src' && find . -name "*.java" > sources.txt && javac -d '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin' -cp '.:/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/jsch-0.1.54.jar' @sources.txt

Run StartRemotePeers:
---------------------
cd '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin' && java -cp '.:/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/jsch-0.1.54.jar' StartRemotePeers

Run PeerProcess Locally:
------------------------
cd '/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin' && java -cp '.:/cise/homes/[username]/CNT5106C_BitTorrent/P2P File Share/bin/jsch-0.1.54.jar' PeerProcess 1001

Run PeerProcess Manually on Remote Machine:
-------------------------------------------
java -cp ".:jsch-0.0.54.jar" PeerProcess 1001
