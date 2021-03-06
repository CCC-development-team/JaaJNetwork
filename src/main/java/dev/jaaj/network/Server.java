/*
 * Copyright 2020 JaaJSoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jaaj.network;


import dev.jaaj.network.exception.ExceptionCannotDisconnect;
import dev.jaaj.network.exception.ExceptionConnectionFailure;
import dev.jaaj.network.exception.ExceptionPortInvalid;
import dev.jaaj.network.exception.ExceptionServerRunnableNotEnded;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Serializable {
    private int port;
    private ServerSocket serverSocket;
    private ServerRunnable runnable;
    private Boolean run;
    private final ExecutorService executorService;

    public Server(int port, ServerRunnable runnable) throws ExceptionPortInvalid {
        if (port < 65536 && port > 0) {
            this.port = port;
            run = false;
        } else {
            throw new ExceptionPortInvalid();
        }
        this.runnable = runnable;
        executorService = Executors.newCachedThreadPool();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        run = true;
        while (run) {
            try {
                Socket client = serverSocket.accept();
                ServerRunnable r;
                r = (ServerRunnable) runnable.clone();
                r.setClientSocket(client);
                executorService.submit(r);
            } catch (SocketException | CloneNotSupportedException i) {
                run = false;
            }
        }
    }


    public void stop() throws ExceptionServerRunnableNotEnded {
        try {
            serverSocket.close();
            Client client = new Client(InetAddress.getByName("127.0.0.1"), port);
            client.connect(); // to make the server crash and stop
            client.disconnect();
        } catch (IOException e) {
            throw new ExceptionServerRunnableNotEnded();
        } catch (ExceptionConnectionFailure | ExceptionCannotDisconnect ignored) {
        } finally {
            run = false;
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }


    public int getPort() {
        return port;
    }
}
