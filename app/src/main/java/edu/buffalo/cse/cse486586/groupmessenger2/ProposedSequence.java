package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.util.Comparator;

import static java.lang.Boolean.FALSE;

public class ProposedSequence implements Serializable {
    String id;
    String msg;
    int seqno;
    boolean deliverable = FALSE;

    public ProposedSequence(String id,String msg,int seqno){
        this.id = id;
        this.msg = msg;
        this.seqno = seqno;
    }

    public String getId() {
        return id;

    }

    public int getSeqno() {
        return this.seqno;
    }

    public String getMsg() {
        return msg;
    }



}
class ProposedSequenceComparator implements Comparator<ProposedSequence>{

    @Override
    public int compare(ProposedSequence o,ProposedSequence o2) {


        // ProposedSequence o = (ProposedSequence) t1;
        //ProposedSequence o2 = (ProposedSequence) t2;

        if (o.seqno > o2.seqno)
            return 1;
        else if (o.seqno < o2.seqno)
            return -1;
        else {
            String id1 = o.getId();
            String id2 = o2.getId();
            String s1[] = id1.split(":");
            String s2[] = id2.split(":");
            if (Integer.parseInt(s1[0]) > Integer.parseInt(s2[0]))
                return 1;
            else {
                return -1;
            }
        }
    }}
