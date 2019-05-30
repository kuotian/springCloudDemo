package com.rule;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import java.util.List;

/**
 * 每个机器访问5次
 */
public class RandomRule_DIY extends AbstractLoadBalancerRule {

    public RandomRule_DIY() {
    }

    private int count = 0; 			// 当前被调用的次数，目前要求每台被调用5次
    private final int MAX_TOTAL = 5;
    private int currentIndex = 0;	// 当前提供服务的机器号

    @SuppressWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE"})
    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        } else {
            Server server = null;

            while(server == null) {
                if (Thread.interrupted()) {
                    return null;
                }

                List<Server> upList = lb.getReachableServers();
                List<Server> allList = lb.getAllServers();
                int serverCount = allList.size();
                if (serverCount == 0) {
                    return null;
                }

                if(count < MAX_TOTAL) {
                    server = upList.get(currentIndex);
                    count++;
                }else {
                    count = 0;
                    currentIndex++;
                    if(currentIndex >= upList.size()) {
                        currentIndex = 0;
                    }
                }


                if (server == null) {
                    Thread.yield();
                } else {
                    if (server.isAlive()) {
                        return server;
                    }

                    server = null;
                    Thread.yield();
                }
            }

            return server;
        }
    }

    public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }

    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}
