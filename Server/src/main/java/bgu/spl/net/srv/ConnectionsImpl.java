package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.Connections;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionsById = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, User> connectionsByUser = new ConcurrentHashMap<>();

    private static ConnectionsImpl instance = null;

    public static ConnectionsImpl getInstance() {
        if (instance == null) {
            instance = new ConnectionsImpl();
        }
        return instance;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (connectionsById.containsKey(connectionId)) {
            connectionsById.get(connectionId).send(msg);
            return true;
        }
        //if the connection ID doesn't exist you should return false and not proceed
        return false;
    }

    @Override
    public void broadcast(T msg) {
        connectionsById.forEach((index,connectionHandler)-> {
            connectionHandler.send(msg);
        });
    }

    @Override
    public void disconnect(int connectionId) {
            connectionsById.remove(connectionId);
            connectionsByUser.remove(connectionId);
    }

    public void addConnection(int idCounter, ConnectionHandler<T> handler) {
        connectionsById.put(idCounter, handler);
    }

    public void addUserForConnection(User user, int idCounter){
        if(connectionsById.containsKey(idCounter)){
            connectionsByUser.put(idCounter,user);
        }
    }

    public boolean checkIfUserLogIn(int connectionId){
        return getConnectionsByUser().containsKey(connectionId);
    }

    public User getUserByConnectionId(int connectionId){
        return connectionsByUser.get(connectionId);
    }

    public int getConnectionIdByUser(User user){
        AtomicInteger val = new AtomicInteger(-1);
        connectionsByUser.forEach((k, v) -> {
            if(v.equals(user)){
                val.set(k);
            }
        });
        return val.get();
    }

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnectionsById() {
        return connectionsById;
    }

    public ConcurrentHashMap<Integer, User> getConnectionsByUser() {
        return connectionsByUser;
    }


}