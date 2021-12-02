import java.util.logging.Logger;

class ZNodeName implements Comparable<ZNodeName>{
	private final String name;
    private String prefix;
    private int sequence = -1;
    private static final Logger LOG = LoggerFactory.getLogger(ZNodeName.class);

    public ZNodeName(String name) {
        if (name == null) {
            throw new NullPointerException("id cannot be null");
        }
        this.name = name;
        this.prefix = name;
        int idx = name.lastIndexOf('-');
        if (idx >= 0) {
            this.prefix = name.substring(0, idx);
            try {
                this.sequence = Integer.parseInt(name.substring(idx + 1));
         
                
            } catch (NumberFormatException e) {
                LOG.info("Number format exception for " + idx);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.info("Array out of bounds for " + idx);
            }
        }
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ZNodeName sequence = (ZNodeName) o;

        if (!name.equals(sequence.name))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + 37;
    }

    public int compareTo(ZNodeName that) {
        int s1 = this.sequence;
        int s2 = that.sequence;
        if (s1 == -1 && s2 == -1) {
            return this.name.compareTo(that.name);
        }
        if (s1 == -1) {
            return -1;
        } else if (s2 == -1) {
            return 1;
        } else {
            return s1 - s2;
        }
    }

    
    public String getName() {
        return name;
    }

    
    public int getZNodeName() {
        return sequence;
    }

    
    public String getPrefix() {
        return prefix;
    }

}
