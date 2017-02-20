/******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 * *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/
package uk.dsxt.remote.instance;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author phd
 */
public class RemoteInstance {
    private final static Logger logger = LogManager.getLogger(RemoteInstance.class);

    private JSch jsch;
    private Session session;
    private ChannelShell channelShell;
    private ChannelSftp channelSftp;

    private String userName;
    private String host;
    private int port;

    //hyperledger specific
    private final static AtomicInteger globalCounter = new AtomicInteger(0);
    private int id;
    private Path logPath;

    public String getHost() {
        return host;
    }

    public int getId() {
        return id;
    }



    public RemoteInstance(String userName, String host, int port, String keyPath, Path logPath) {
        this.userName = userName;
        this.host = host;
        this.port = port;
        this.id = globalCounter.getAndIncrement();
        this.logPath = logPath;
        jsch = new JSch();
        try {
            jsch.addIdentity(keyPath);
        } catch (JSchException e) {
            logger.error(e);
        }
    }

    public Session getOrCreateSession() throws JSchException{
        if (session == null || !session.isConnected()) {
            session = jsch.getSession(userName, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setDaemonThread(true);
            session.connect();
        }
        return session;
    }

    public ChannelShell getOrCreateChannelShell() throws JSchException {
        if (channelShell == null || channelShell.isClosed()) {
            channelShell = (ChannelShell) getOrCreateSession().openChannel("shell");
        }
        return channelShell;
    }

    public ChannelSftp getOrCreateChannelSftp() throws JSchException {
        if (channelSftp == null || channelSftp.isClosed()) {
            channelSftp = (ChannelSftp) getOrCreateSession().openChannel("sftp");
        }
        return channelSftp;
    }

    public boolean sendCommands(List<String> commands) {
        try (FileOutputStream logStream = new FileOutputStream(logPath.resolve(host + "_deploy.log").toFile(), true)){
            ChannelShell channel = getOrCreateChannelShell();
            logger.debug("Executing commands on: " + channel.getSession().getHost());
            channelShell.setOutputStream(logStream);
            PrintStream shellStream = new PrintStream(channel.getOutputStream());
            channel.connect();
            commands.add("exit");
            commands.add("exit");
            for (String command : commands) {
                logger.debug("Executing command on: " + channel.getSession().getHost());
                shellStream.println(command);
                shellStream.flush();
            }
            while(channel.isConnected())
            {
                logger.info("----- Executing commads... ----");
                Thread.sleep(10000);
            }
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        finally {
            channelShell.disconnect();
            session.disconnect();
            session = null;
        }
        return false;
    }

    public boolean uploadFiles(List<Path> files) {
        try {
            ChannelSftp channel = getOrCreateChannelSftp();
            logger.debug("Uploading files to: " + channel.getSession().getHost());
            channel.connect();
            files.forEach(f -> {
                try (FileInputStream fis = new FileInputStream(f.toFile())) {
                    channel.put(fis, f.getFileName().toString());
                    logger.info("File {} uploaded to: {}", f.getFileName().toString(), channel.getSession().getHost());
                } catch (Exception e) {
                    logger.error(e);
                }
            });
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        finally {
            channelSftp.disconnect();
        }
        return false;
    }

    public boolean downloadFiles(List<Path> from, Path to) {
        try {
            ChannelSftp channel = getOrCreateChannelSftp();
            logger.debug("Download files from: " + channel.getSession().getHost());
            channel.connect();
            for (Path file: from) {
                channel.get(file.toString(), to.toString());
            }
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        finally {
            channelSftp.disconnect();
        }
        return false;
    }
}
