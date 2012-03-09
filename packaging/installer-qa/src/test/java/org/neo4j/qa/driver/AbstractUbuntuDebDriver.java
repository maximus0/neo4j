/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.qa.driver;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.neo4j.vagrant.Shell.Result;
import org.neo4j.vagrant.VirtualMachine;

public abstract class AbstractUbuntuDebDriver extends AbstractPosixDriver {

    private static final String INSTALL_DIR = "/var/lib/neo4j";
    private String installerPath;
    private String installerFileName;

    public AbstractUbuntuDebDriver(VirtualMachine vm, String installerPath)
    {
        super(vm);
        this.installerPath = installerPath;
        this.installerFileName = new File(installerPath).getName();
    }
    
    @Override
    public void up() {
        super.up();
    }
    
    @Override
    public void runInstall() {
        vm.copyFromHost(installerPath, "/home/vagrant/" + installerFileName);
        sh.run("sudo dpkg -i " + installerFileName);
        sh.run("sudo chmod -R 777 " + INSTALL_DIR + "/conf");
    }
    
    @Override
    public void runUninstall() {
        sh.run("sudo dpkg -r neo4j");
    }
    
    @Override
    public void startService() {
        Result r = sh.run("sudo /etc/init.d/neo4j-service start");
        if(r.getOutput().contains("BAD.")) {
            throw new RuntimeException("Starting neo4j service failed on ["+vm.definition().ip()+"]");
        }
    }
    
    @Override
    public void stopService() {
        Result r = sh.run("sudo /etc/init.d/neo4j-service stop");
        if(r.getOutput().endsWith("done")) {
            throw new RuntimeException("Stopping neo4j service failed on ["+vm.definition().ip()+"]");
        }
    }    
    
    @Override
    public String installDir() {
        return INSTALL_DIR;
    }
    
    @Override
    public void downloadLogsTo(String target) {
        String ip = vm().definition().ip();
        System.out.println("Downloading logs for server " + ip + " to " + target + ".");
        downloadLog(installDir() + "/data/graph.db/messages.log", target + "/" + ip + "-messages.log");
        downloadLog(installDir() + "/data/log/neo4j.0.0.log", target + "/" + ip + "-neo4j.0.0.log");
    }
    
    protected void downloadLog(String from, String to)
    {
        if( ! sh.run("ls " + from).getOutput().contains("No such file")) {
            vm().copyFromVM(from, to);
        } else {
            try
            {
                FileUtils.writeStringToFile(new File(to), "This log file did not exist on the VM.");
            } catch (IOException e1)
            {   
                throw new RuntimeException(e1);
            }
        }
    }
}
