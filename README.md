# big data multicast
大数据组播工具,内部实现好拆包装包算法.使用该工具可直接发送大数据包和接收.几行代码就搞定!

# 如何开始?
  1.把 multicast-1.0.jar 添加到您项目的classpath中去.
  2.编写一个类实现 MessageHandler 接口
  3.使用TiandeMulticastSocket关键类开启组播服务:

    TiandeMulticastSocket tiandeMulticastSocket = new TiandeMulticastSocket();
    tiandeMulticastSocket.setGroup("228.0.0.10");
    tiandeMulticastSocket.setIntervalSend(0);
    tiandeMulticastSocket.setReceiveTimeOut(2000);
    tiandeMulticastSocket.setBlockMaxSize(1024 * 50);
    tiandeMulticastSocket.setReceivePacketSize(1024 * 60);	
    tiandeMulticastSocket.registerHandler(handler);
    tiandeMulticastSocket.start();

  4.发送组播消息:

    TiandeMulticastMessage msg = new TiandeMulticastMessage();
    msg.setMethod("register");
    msg.setProperty("key", "value");
    msg.setBody("Hello a big data now");
    tiandeMulticastSocket.send(msg);