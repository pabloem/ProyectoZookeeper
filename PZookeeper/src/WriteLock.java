
import java.security.acl.Acl;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

public class WriteLock extends ProtocolSupport {
	private static final Logger LOG = LoggerFactory.getLogger(WriteLock.class);

	private final String dir;
    private String id;
    private ZNodeName idName;
    private String ownerId;
    private String lastChildId;
    private byte[] data = {0x12, 0x34};
    private LockListener callback;
    private LockZooKeeperOperation zop;
	
    public WriteLock(ZookeeperNode zookeeper, String dir, List<Acl> acl) {
        super(zookeeper);
        this.dir = dir;
        if (acl != null) {
            setAcl(acl);
        }
        this.zop = new LockZooKeeperOperation();
    }
    private void setAcl(List<Acl> acl) {
		// TODO Auto-generated method stub
		
	}
	public WriteLock(ZookeeperNode zookeeper, String dir, List<Acl> acl, 
            LockListener callback) {
        this(zookeeper, dir, acl);
        this.callback = callback;
    }

    
    public LockListener getLockListener() {
        return this.callback;
    }

    public void setLockListener(LockListener callback) {
        this.callback = callback;
    }

    
    public synchronized void unlock() throws RuntimeException {

        if (!isClosed() && id != null) {
            try {
                ZooKeeperOperation zopdel = new ZooKeeperOperation() {
                    public boolean execute() throws KeeperException,
                        InterruptedException {
                        zookeeper.delete(id, -1);   
                        return Boolean.TRUE;
                    }
                };
                zopdel.execute();
            } catch (InterruptedException e) {
                LOG.warn("Caught: " + e, e);
                
               Thread.currentThread().interrupt();
            } catch (KeeperException.NoNodeException e) {
                // do nothing
            } catch (KeeperException e) {
                LOG.warn("Caught: " + e, e);
                throw (RuntimeException) new RuntimeException(e.getMessage()).
                    initCause(e);
            }
            finally {
                if (callback != null) {
                    callback.lockReleased();
                }
                id = null;
            }
        }
    }

    /** 
     * el reloj llama
     */
    private class LockWatcher implements Watcher {
        public void process(WatchedEvent event) {
            
            LOG.debug("Watcher fired on path: " + event.getPath() + " state: " + 
                    event.getState() + " type " + event.getType());
            try {
                lock();
            } catch (Exception e) {
                LOG.warn("Failed to acquire lock: " + e, e);
            }
        }
    }
    private  class LockZooKeeperOperation implements ZooKeeperOperation {

        
        private void findPrefixInChildren(String prefix, ZooKeeper zookeeper, String dir) 
            throws KeeperException, InterruptedException {
            List<String> names = zookeeper.getChildren(dir, false);
            for (String name : names) {
                if (name.startsWith(prefix)) {
                    id = dir + "/" + name;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found id created last time: " + id);
                    }
                    break;
                }
            }
            if (id == null) {
                id = zookeeper.create(dir + "/" + prefix, data, 
                        getAcl(), EPHEMERAL_SEQUENTIAL);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created id: " + id);
                }
            }

        }

        
        public boolean execute() throws KeeperException, InterruptedException {
            do {
                if (id == null) {
                    long sessionId = zookeeper.getSessionId();
                    String prefix = "x-" + sessionId + "-";
                    
                    findPrefixInChildren(prefix, zookeeper, dir);
                    idName = new ZNodeName(id);
                }
                if (id != null) {
                    List<String> names = zookeeper.getChildren(dir, false);
                    if (names.isEmpty()) {
                        LOG.warn("No children in: " + dir + " when we've just " +
                        "created one! Lets recreate it...");
                        
                        id = null;
                    } else {
                        
                        SortedSet<ZNodeName> sortedNames = new TreeSet<ZNodeName>();
                        for (String name : names) {
                            sortedNames.add(new ZNodeName(dir + "/" + name));
                        }
                        ownerId = sortedNames.first().getName();
                        SortedSet<ZNodeName> lessThanMe = sortedNames.headSet(idName);
                        if (!lessThanMe.isEmpty()) {
                            ZNodeName lastChildName = lessThanMe.last();
                            lastChildId = lastChildName.getName();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("watching less than me node: " + lastChildId);
                            }
                            Stat stat = zookeeper.exists(lastChildId, new LockWatcher());
                            if (stat != null) {
                                return Boolean.FALSE;
                            } else {
                                LOG.warn("Could not find the" +
                                        " stats for less than me: " + lastChildName.getName());
                            }
                        } else {
                            if (isOwner()) {
                                if (callback != null) {
                                    callback.lockAcquired();
                                }
                                return Boolean.TRUE;
                            }
                        }
                    }
                }
            }
            while (id == null);
            return Boolean.FALSE;
        }
    };

    
     
    public synchronized boolean lock() throws KeeperException, InterruptedException {
        if (isClosed()) {
            return false;
        }
        ensurePathExists(dir);

        return (Boolean) retryOperation(zop);
    }

    
    public String getDir() {
        return dir;
    }

    
    public boolean isOwner() {
        return id != null && ownerId != null && id.equals(ownerId);
    }

    
    public String getId() {
       return this.id;
    }

}
