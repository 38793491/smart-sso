package com.smart.sso.server.common;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.smart.sso.client.dto.RpcUserDto;

/**
 * 本地票据管理
 * 
 * @author Joe
 */
public class LocalTicketGrantingTicketManager extends TicketGrantingTicketManager {

    private final Map<String, DummyTgt> tgtMap = Maps.newConcurrentMap();

    @Override
    public String generate(RpcUserDto user) {
        String tgt = "TGT-" + UUID.randomUUID().toString().replaceAll("-", "");
        
        DummyTgt dummyTgt = new DummyTgt();
        dummyTgt.user = user;
        dummyTgt.expired = new Date(new Date().getTime() + timeout * 1000);
        tgtMap.put(tgt, dummyTgt);
        return tgt;
    }
    
    @Override
    public RpcUserDto validate(String tgt) {
        DummyTgt dummyTgt = tgtMap.get(tgt);
        if (dummyTgt == null || new Date().getTime() > dummyTgt.expired.getTime()) {
            return null;
        }
        return dummyTgt.user;
    }
    
    @Override
    public void remove(String tgt) {
        tgtMap.remove(tgt);
    }

    @Override
    public void verifyExpired() {
        Date now = new Date();
        for (Entry<String, DummyTgt> entry : tgtMap.entrySet()) {
            String tgt = entry.getKey();
            DummyTgt dummyTgt = entry.getValue();
            // 已过期
            if (now.compareTo(dummyTgt.expired) > 0) {
                tgtMap.remove(tgt);
                logger.debug("TGT : " + tgt + "已失效");
            }
        }
    }
    
    private class DummyTgt {
        private RpcUserDto user;
        private Date expired; // 过期时间
    }
}
