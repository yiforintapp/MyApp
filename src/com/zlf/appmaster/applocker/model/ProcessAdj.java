package com.zlf.appmaster.applocker.model;


/**
 * 进程oom_adj
 * @author Jasper
 *
 */
public class ProcessAdj {
    public String user = "";
    public String pkg = "";
    public int ppid;
    public int pid;
    public int oomAdj;
    public String shortActivity = "";
    // 进程调度优先级
    public boolean isFg;
    
//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof ProcessAdj) || o == null) return false;
//
//        ProcessAdj adj = (ProcessAdj) o;
//        if (pkg == null) return false;
//
//        return pkg.equals(adj.pkg) && pid == adj.pid;
//    }

    @Override
    public String toString() {
        return "ProcessAdj: user-" + user + ", pkg-" + pkg + ", adj-" + oomAdj;
    }
    
    
}
