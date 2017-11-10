
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

    private String ip;
    private int port;
    private String username;

    private DatagramSocket receiveSock;
    private DatagramSocket sendSock;

    private int serverPort;
    private InetAddress serverAddress;

    private ArrayList<String> files;
    private BlockingQueue<Neighbour> neighbourTable;
    private BlockingQueue<DatagramPacket> messagesQueue;
    private Thread listeningThread;
    private Thread executionThread;
    private Map<String, Integer> map = new HashMap<String, Integer>();
    Timer timer2;

    private boolean stop;

    ArrayList<Object[]> results = new ArrayList<>();

    public ArrayList<Object[]> getResults() {
        return results;
    }

    public void setResults(ArrayList<Object[]> results) {
        this.results = results;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public DatagramSocket getReceiveSock() {
        return receiveSock;
    }

    public DatagramSocket getSendSock() {
        return sendSock;
    }

    public int getServerPort() {
        return serverPort;
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public List<String> getFiles() {
        return files;
    }

    public BlockingQueue<Neighbour> getNeighbourTable() {
        return neighbourTable;
    }

    public BlockingQueue<DatagramPacket> getMessagesQueue() {
        return messagesQueue;
    }

    public Thread getListeningThread() {
        return listeningThread;
    }

    public Thread getExecutionThread() {
        return executionThread;
    }

    public boolean isStop() {
        return stop;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setReceiveSock(DatagramSocket receiveSock) {
        this.receiveSock = receiveSock;
    }

    public void setSendSock(DatagramSocket sendSock) {
        this.sendSock = sendSock;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }

    public void setNeighbourTable(BlockingQueue<Neighbour> neighbourTable) {
        this.neighbourTable = neighbourTable;
    }

    public void setMessagesQueue(BlockingQueue<DatagramPacket> messagesQueue) {
        this.messagesQueue = messagesQueue;
    }

    public void setListeningThread(Thread listeningThread) {
        this.listeningThread = listeningThread;
    }

    public void setExecutionThread(Thread executionThread) {
        this.executionThread = executionThread;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public Node(String ip, int port, String username, ArrayList<String> files, int serverPort, String sAddress) {

        try {
            stop = false;
            receiveSock = new DatagramSocket(port);
            sendSock = null;
            messagesQueue = new LinkedBlockingQueue<DatagramPacket>();

            this.ip = ip;
            this.port = port;
            this.username = username;
            this.files = files;
            this.serverPort = serverPort;
            this.serverAddress = InetAddress.getByName(sAddress);
            neighbourTable = new LinkedBlockingDeque<Neighbour>();

            listeningThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (!stop) {
                        byte[] buffer = new byte[65536];
                        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                        try {
                            receiveSock.receive(incoming);
                        } catch (IOException e) {
                           // e.printStackTrace();
                        }

                        try {
                            messagesQueue.put(incoming);
                        } catch (InterruptedException e) {
                           // e.printStackTrace();
                        }
                    }
                }
            });

            executionThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    while (!stop) {

                        DatagramPacket incoming = null;
                        try {
                            incoming = messagesQueue.take();
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                           // e1.printStackTrace();
                        }

                        byte[] data = incoming.getData();
                        String s = new String(data, 0, incoming.getLength());

                        String[] values = s.split(" ");
                        String command = values[1];

                        switch (command) {

                            case "REGOK":
                                String noOfNodes = values[2];

                                if (noOfNodes.equals("0")) {
                                } else if (noOfNodes.equals("1")) {
                                    String neighbourIp = values[3];
                                    int neighbourPort = Integer.parseInt(values[4]);
                                    String join_request = "JOIN " + ip + " " + port + " " + neighbourTable.size();
                                    try {
                                        sendMsg(join_request, InetAddress.getByName(neighbourIp), neighbourPort);
                                    } catch (UnknownHostException e) {
                                        // TODO Auto-generated catch block
                                       // e.printStackTrace();
                                    }
                                } else if (noOfNodes.equals("2")) {
                                    String neighbourIp = values[3];
                                    int neighbourPort = Integer.parseInt(values[4]);

                                    String join_request = "JOIN " + ip + " " + port + " " + neighbourTable.size();
                                    try {
                                        sendMsg(join_request, InetAddress.getByName(neighbourIp), neighbourPort);
                                    } catch (UnknownHostException e) {
                                        // TODO Auto-generated catch block
                                       // e.printStackTrace();
                                    }

                                    neighbourIp = values[5];
                                    neighbourPort = Integer.parseInt(values[6]);

                                    try {
                                        sendMsg(join_request, InetAddress.getByName(neighbourIp), neighbourPort);
                                    } catch (UnknownHostException e) {
                                        // TODO Auto-generated catch block
                                      //  e.printStackTrace();
                                    }
                                } else {

                                }
                                break;

                            case "JOIN":
                                int value;
                                if (neighbourTable.size() < 4) {
                                    Neighbour neighbour = new Neighbour(values[2], Integer.parseInt(values[3]), Integer.parseInt(values[4]));
                                    neighbourTable.add(neighbour);
                                    value = 0;
                                } else {
                                    value = 9999;
                                }

                                String join_reply = "JOINOK " + value + " " + neighbourTable.size() + " " + port;
                                sendMsg(join_reply, incoming.getAddress(), Integer.parseInt(values[3]));
                                break;

                            case "JOINOK":
                                if (values[2].equals("0")) {
                                    Neighbour neighbour = new Neighbour(incoming.getAddress().getHostName(), Integer.parseInt(values[4]), values[3]);
                                    neighbourTable.add(neighbour);
                                }
                                break;

                            case "LEAVE":
                                for (Neighbour neighbour_leave : neighbourTable) {
                                    if (neighbour_leave.getIp().equals(values[2])) {
                                        neighbourTable.remove(neighbour_leave);
                                    }
                                }

                                int value_leave = 0;// change response by changing this variable

                                String leave_reply = "LEAVEOK " + value_leave;
                                sendMsg(leave_reply, incoming.getAddress(), Integer.parseInt(values[3]));
                                break;

                            case "LEAVEOK":
                                if (values[2].equals("0")) {
                                    unreg();
                                }
                                break;

                            case "UNROK":
                                if (values[2].equals("0")) {
                                    stopNode();
                                }
                                break;

                            case "SER":
                                Integer noHops = Integer.parseInt(values[5]);

                                if ((values[2].equals(ip) && port == Integer.parseInt(values[3])) || noHops > 5) {
                                    //System.out.println("No of hops exceeded");
                                } else {
                                    String query = values[4];
                                    
                                    ArrayList<String> result = new ArrayList<>();
                                    String queryWord[] = query.toLowerCase().split(" ");
                                    List<String> queryWords = new ArrayList<>();
                                    Collections.addAll(queryWords, queryWord);

                                    ArrayList<String> myFiles = files;

                                    for(String file: myFiles) {
                                            List<String> subStrings = new ArrayList<>();
                                            Collections.addAll(subStrings, file.toLowerCase().split(" "));
                                            boolean check=subStrings.retainAll(queryWords);
                                            if(!check || (subStrings.size() > 0)){
                                                    result.add(file);
                                            }
                                    }

                                  
                                    if (result.size() > 0) {
                                        String search_reply = "SEROK " + result.size() + " " + ip + " " + port + " " + (noHops + 1);
                                        for (String fileName : result) {
                                            search_reply += (" " + fileName.replace(' ', '_'));
                                        }
                                        try {
                                            sendMsg(search_reply, InetAddress.getByName(values[2]), Integer.parseInt(values[3]));
                                        } catch (NumberFormatException e) {
                                          //  e.printStackTrace();
                                        } catch (UnknownHostException e) {
                                          //  e.printStackTrace();
                                        }
                                    } else {

                                        for (Neighbour neighbour : neighbourTable) {
                                            String search_request = "SER " + values[2] + " " + values[3] + " " + values[4] + " " + (noHops + 1);
                                            try {

                                                sendMsg(search_request, InetAddress.getByName(neighbour.getIp()), neighbour.getPort());
                                            } catch (UnknownHostException e) {
                                               // e.printStackTrace();
                                            }

                                        }
                                    }
                                }
                                break;

                            case "SEROK":
                                String response = "File found, ip = " + values[3] + ", port = " + values[4] + ", no of hops = " + values[5] + ", files = ";

                                for (int i = 6; i < values.length; i++) {
                                    response += (values[i] + ", ");
                                    Object[] o = {values[3] + ":" + values[4], values[i], values[5]};
                                    results.add(o);
                                }
                                System.out.println(response);
                                break;

                        }

                    }

                }
            });

            listeningThread.start();
            executionThread.start();

            reg();

        } catch (UnknownHostException e) {
           // e.printStackTrace();

        } catch (SocketException e1) {
          //  e1.printStackTrace();
        }

    }

    public void reg() {

        try {
            String init_request = "REG " + ip + " " + port + " " + username;
            int length = init_request.length() + 5;
            init_request = String.format("%04d", length) + " " + init_request;
            DatagramPacket regrequest = new DatagramPacket(init_request.getBytes(), init_request.getBytes().length,
                    serverAddress, serverPort);
            receiveSock.send(regrequest);
            startHeartBeat();
        } catch (SocketException e1) {
           // e1.printStackTrace();
        } catch (IOException e) {
          //  e.printStackTrace();
        }

    }

    public void unreg() {
        try {
            String unreg_request = "UNREG " + ip + " " + port + " " + username;
            int length = unreg_request.length() + 5;
            unreg_request = String.format("%04d", length) + " " + unreg_request;

            DatagramPacket unregrequest = new DatagramPacket(unreg_request.getBytes(), unreg_request.getBytes().length,
                    serverAddress, serverPort);
            receiveSock.send(unregrequest);
            receiveSock.close();
            sendSock.close();

        } catch (UnknownHostException e) {
            receiveSock.close();
            sendSock.close();
           // e.printStackTrace();
        } catch (SocketException e1) {
            receiveSock.close();
            sendSock.close();
          //  e1.printStackTrace();
        } catch (IOException e) {
            receiveSock.close();
            sendSock.close();
           // e.printStackTrace();
        }
        stopNode();
    }

    public void leave() {
        try {
            for (Neighbour neighbour : neighbourTable) {
                sendSock = new DatagramSocket(port+1);
                String leave_request = "LEAVE " + ip + " " + port;

                int length = leave_request.length() + 5;

                leave_request = String.format("%04d", length) + " " + leave_request;
                
                DatagramPacket leaveRequest = new DatagramPacket(leave_request.getBytes(), leave_request.getBytes().length,
                        InetAddress.getByName(neighbour.getIp()), neighbour.getPort());
                sendSock.send(leaveRequest);
                sendSock.close();
            }

        } catch (UnknownHostException e) {
            sendSock.close();
          //  e.printStackTrace();

        }  catch (IOException e) {
            sendSock.close();
          //  e.printStackTrace();
        }

    }

    public void search(String query) {
        
        results.clear();
        
        ArrayList<String> result = new ArrayList<>();
        String values[]=query.toLowerCase().split(" ");
        List<String> queryWords = new ArrayList<>();
        Collections.addAll(queryWords, values);

        ArrayList<String> myFiles = files;

        for(String file: myFiles) {
                List<String> subStrings = new ArrayList<>();
                Collections.addAll(subStrings, file.toLowerCase().split(" "));
                boolean check=subStrings.retainAll(queryWords);
                if(!check || (subStrings.size() > 0)){
                        result.add(file);
                }
        }
			
        String response = null;

        if (result.size() > 0) {
            response = "File found in first node, ip = " + ip + ", port = " + port + " no of hops = 1, files = ";
            for (String fileName : result) {
                response += (fileName + ", ");
                Object[] o = {ip + ":" + port, fileName, 1};
                results.add(o);
            }
            System.out.println(response);
        }
            
            
    
        for (Neighbour neighbour : neighbourTable) {
            String search_request = "SER " + ip + " " + port + " " + query + " " + 1;
            int length_search = search_request.length() + 5;

            try {
                sendMsg(search_request, InetAddress.getByName(neighbour.getIp()), neighbour.getPort());
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            }

        }
        

    }

    public boolean sendMsg(String request, InetAddress receiverIp, int receiverPort) {

        boolean send = true;
        int length = request.length() + 5;
        request = String.format("%04d", length) + " " + request;
        DatagramPacket regrequest;
        try {
            sendSock = new DatagramSocket(this.port + 1);
            regrequest = new DatagramPacket(request.getBytes(), request.getBytes().length, receiverIp, receiverPort);
            sendSock.send(regrequest);
            sendSock.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            sendSock.close();
            send = false;
           // e.printStackTrace();

        }
        return send;

    }

    public void echoFiles() {
        System.out.println("Files in this node: ");
        for (String fileName : files) {
            System.out.println(fileName + " ");
        }
    }

    public void echoNeighbours() {
        System.out.println("Neighbours of this node: ");
        for (Neighbour neighbour : neighbourTable) {
            System.out.println(neighbour.getIp() + " " + neighbour.getPort() + neighbour.getUsername() + neighbour.getNoOfPeers());
        }
    }

    // simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }

    public void stopNode() {
        stop = true;
    }

//    public void startHeartBeat() {
//        timer2 = new Timer();
//        timer2.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                
//                neighbourTable.forEach(e->{
//                    String ipPort = e.getIp()+":"+e.getPort();
//                    Integer val =map.get(ipPort);
//                    if (val!=null && val>4){
//                        neighbourTable.remove(e);
//                        map.remove(ipPort);
//                    }
//                });
//                
//            }
//        }, 5 * 1000, 10 * 1000);
//    }
    public void startHeartBeat() {
        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {

                neighbourTable.forEach(e -> {

                    int count = 0;
                    boolean send = false;
                    
                    while (!send && count <5) {
                        try {
                            send = sendMsg("check", InetAddress.getByName(e.getIp()), e.getPort());
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        count++;
                    }
                    if(count==5){
                        neighbourTable.remove(e);
                    }
                });

            }
        }, 5 * 1000, 10 * 1000);
    }

}

