import java.net.URL;
import java.text.SimpleDateFormat;

import com.vmware.vim25.HostHardwareInfo;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.*;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import java.util.Date;

public class CS218_HW2_STHENTU {

	public static void main(String[] args) throws Exception
	{
		if(args.length != 3) {
			System.out.println("Usage : cmd ip login password");
		}
		System.out.println("\n");
		System.out.println("CS 218 Fall 2020 HW2 from Siddartha Thentu");
		ServiceInstance si = new ServiceInstance(new URL("https://"+args[0]+"/sdk"),args[1],args[2],true);
		Folder rootFolder = si.getRootFolder();
		String productFullName = si.getAboutInfo().getFullName();

		ManagedEntity[] hostEntities = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(hostEntities==null || hostEntities.length==0) {
			return;
		}
		int i=0;
		for(ManagedEntity he : hostEntities) {
			HostSystem hostsystemobj = (HostSystem) he;
			System.out.println(String.format("host[%d]:",i));
			System.out.println("Name = "+hostsystemobj.getName());
			System.out.println("ProductFullName = "+productFullName);
			int j=0;
			for(Datastore ds : hostsystemobj.getDatastores()) {
				System.out.print(String.format("Datastore[%d]:  ",j));
				System.out.print("name="+ds.getName()+", Capacity = "+ds.getSummary().getCapacity()/(1024.0*1024.0*1024.0)+" GB,"+" Freespace = "+ds.getSummary().getFreeSpace()/(1024.0*1024.0*1024.0)+" GB");
				System.out.println();
				j+=1;
			}
			int k=0;
			for(Network nw : hostsystemobj.getNetworks()) {
				System.out.print(String.format("Network[%d] :",k));
				System.out.print(" name="+nw.getName());
				System.out.println();
				k+=1;
			}
			i+=1;
		}
		
		System.out.println("\n");
		
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		
		if(mes==null || mes.length==0) {
			return;
		}
		
		int l=0;
		for(ManagedEntity me: mes) {
			System.out.println(String.format("VM[%d]:",l));
			VirtualMachine vm = (VirtualMachine) me;
			VirtualMachineConfigInfo vminfo = vm.getConfig();
			VirtualMachineCapability vmc = vm.getCapability();
			System.out.println("Name = "+vm.getName());
			System.out.println("GuestOs = "+vminfo.getGuestFullName());
			System.out.println("CPUs = "+vminfo.getHardware().getNumCPU());
			System.out.println("Memory = "+vminfo.getHardware().getMemoryMB()+" MB");
			System.out.println("Guest state = "+vm.getGuest().guestState);
			System.out.println("IPAddress = "+vm.getGuest().ipAddress);
			System.out.println("Tool running state = "+vm.getGuest().getToolsRunningStatus());
			System.out.println("Power state = "+vm.getRuntime().getPowerState());
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Task task = vm.createSnapshot_Task(formatter.format(new Date()),"sthentu-074",false,false);
			System.out.print("Snapshot VM: ");
			if(task.waitForTask().equals("success")) {
				System.out.print(" status = "+task.waitForTask()+", start time = "+formatter.format(task.getTaskInfo().getStartTime().getTime())+", completion time = "+formatter.format(task.getTaskInfo().getCompleteTime().getTime()));
			}
			else {
				System.out.print(" status = "+task.getTaskInfo().getError().getLocalizedMessage());
			}
			System.out.println();
			Task task2 = null;
			
			VirtualMachinePowerState state = vm.getRuntime().getPowerState();
			if(state.toString().equals("poweredOn")) {
				task2 = vm.powerOffVM_Task();
				System.out.print("Power off VM: ");
				if(task2.waitForTask().equals("success")) {
					System.out.print(" status = "+task2.waitForTask()+", start time = "+formatter.format(task2.getTaskInfo().getStartTime().getTime())+", completion time = "+formatter.format(task2.getTaskInfo().getCompleteTime().getTime()));
				}
				else {
					System.out.print(" status = "+task2.getTaskInfo().getError().getLocalizedMessage());
				}
			}
			
			else if(state.toString().equals("poweredOff")) {
				task2 = vm.powerOnVM_Task(null);
				System.out.print("Power on VM: ");
				if(task2.waitForTask().equals("success")) {
					System.out.print(" status = "+task2.waitForTask()+", start time = "+formatter.format(task2.getTaskInfo().getStartTime().getTime())+", completion time = "+formatter.format(task2.getTaskInfo().getCompleteTime().getTime()));
				}
				else {
					System.out.print(" status = "+task2.getTaskInfo().getError().getLocalizedMessage());
				}
			}
			System.out.println("\n");
			l+=1;	
		}
		si.getServerConnection().logout();
	}
}
