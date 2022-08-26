/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.polaris.sync.config.plugins.kubernetes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;
import javax.net.SocketFactory;
import okhttp3.OkHttpClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class KubernetesClient {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesClient.class);

    private static InetAddress LOCAL_ADDRESS = null;

    static {
        String interName = System.getenv("POLARIS_SYNC_NETWORK_INTERFACE");
        if (!StringUtils.isBlank(interName)) {
            try {
                NetworkInterface nif = NetworkInterface.getByName(interName);
                Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
                LOCAL_ADDRESS = nifAddresses.nextElement();
                LOG.info("[ConfigProvider][Kubernetes] find network interface : {} address : {}", interName, LOCAL_ADDRESS);
            } catch (SocketException e) {
                LOG.error("[ConfigProvider][Kubernetes] find network interface : {} fail", interName, e);
            }
        }
    }

    public static OkHttpClient buildOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .socketFactory(new SocketFactory() {
                    @Override
                    public Socket createSocket(String host, int port) throws IOException {
                        if (Objects.isNull(LOCAL_ADDRESS)) {
                            return new Socket(host, port);
                        }
                        return new Socket(host, port, LOCAL_ADDRESS, 0);
                    }

                    @Override
                    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
                            throws IOException {
                        if (Objects.isNull(LOCAL_ADDRESS)) {
                            return new Socket(host, port, localHost, localPort);
                        }
                        return new Socket(host, port, LOCAL_ADDRESS, localPort);
                    }

                    @Override
                    public Socket createSocket(InetAddress host, int port) throws IOException {
                        if (Objects.isNull(LOCAL_ADDRESS)) {
                            return new Socket(host, port);
                        }
                        return new Socket(host, port, LOCAL_ADDRESS, 0);
                    }

                    @Override
                    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
                            throws IOException {
                        if (Objects.isNull(LOCAL_ADDRESS)) {
                            return new Socket(address, port, localAddress, localPort);
                        }
                        return new Socket(address, port, LOCAL_ADDRESS, localPort);
                    }
                })
                .build();

        return okHttpClient;
    }

}
