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
package uk.dsxt.bb.remote.instance;

import com.jcraft.jsch.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    @Getter @Setter private boolean isRunning;

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
        this.isRunning = true;
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
            for (String command : commands) {
                logger.debug("Executing command on: " + channel.getSession().getHost());
                shellStream.println(command);
                shellStream.flush();
            }
            while(channel.isConnected())
            {
                logger.info("----- Executing commads... ----");
                Thread.sleep(10000);
                shellStream.println("exit");
                shellStream.flush();
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

    public boolean uploadFolder(Path src, String dst) {
        try {
            ChannelSftp channel = getOrCreateChannelSftp();
            logger.debug("Uploading folders to: " + channel.getSession().getHost());
            channel.connect();
            uploadFolder(channel, src, dst);
            logger.info("Folder {} uploaded to: {}", src.getFileName().toString(), channel.getSession().getHost());
        } catch (SftpException | JSchException e) {
            logger.error(e);
        } finally {
            channelSftp.disconnect();
        }
        return false;
    }

    private void uploadFolder(ChannelSftp openedChannel, Path src, String dst) throws SftpException {
        openedChannel.mkdir(dst);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
            for (Path path: ds) {
                if (path.toFile().isDirectory()) {
                    uploadFolder(openedChannel, path, dst + "/" + path.getFileName().toString());
                } else {
                    openedChannel.put(path.toString(), dst + "/" + path.getFileName().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean downloadFiles(List<String> from, Path to) {
        try {
            ChannelSftp channel = getOrCreateChannelSftp();
            logger.debug("Download files from: " + channel.getSession().getHost());
            channel.connect();
            for (String file: from) {
                channel.get(file, to.toString());
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

    public boolean downloadFolder(String src, Path dst) {
        try {
            if (!dst.toFile().exists()) {
                dst.toFile().mkdirs();
            }
            ChannelSftp channel = getOrCreateChannelSftp();
            logger.debug("Download folder from: " + channel.getSession().getHost());
            channel.connect();
            src = src.replace("~", channel.getHome());
            downloadFolder(channel, src, dst);
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        finally {
            channelSftp.disconnect();
        }
        return false;
    }

    private void downloadFolder(ChannelSftp openedChannel, String src, Path dst) throws SftpException {
        if (!dst.toFile().exists()) {
            dst.toFile().mkdirs();
        }
        List<ChannelSftp.LsEntry> files = new ArrayList<>();
        openedChannel.ls(src, entry -> {
            files.add(entry);
            return ChannelSftp.LsEntrySelector.CONTINUE;
        });
        for (ChannelSftp.LsEntry file: files) {
            if (file.getFilename().equals(".") || file.getFilename().equals("..")) {
                continue;
            }
            if (file.getAttrs().isDir()) {
                downloadFolder(openedChannel,src + "/" + file.getFilename(), dst.resolve(file.getFilename()));
            } else {
                openedChannel.get(src + "/" + file.getFilename(), dst.resolve(file.getFilename()).toString());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof RemoteInstance)) return false;
        RemoteInstance other = (RemoteInstance) o;
        return host.equals(other.host);
    }

    @Override
    public int hashCode() {
        return host.hashCode();
    }
}
