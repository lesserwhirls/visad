/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.Accessor;
import ucar.multiarray.MultiArray;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.lang.reflect.InvocationTargetException;


/**
 * A concrete implementation of the Netcdf interface,
 * this class uses java rmi to access a remote Netcdf.
 * <p>
 * 
 * @see Netcdf
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:44:21 $
 */
public class
RemoteNetcdf
	extends AbstractNetcdf
{

	/**
	 * Get remote dataset directory
	 * service from a given host.
	 * Convience function which wraps java.rmi.Naming.lookup().
	 * @param remoteHostName String host name or dotted quad
	 * @return NetcdfService
	 */
	static public NetcdfService
	getNetcdfService(String remoteHostName)
			throws RemoteException,
				java.rmi.NotBoundException,
				java.net.MalformedURLException
	{
		String svcName = "//" + remoteHostName + "/"
			+ NetcdfService.SVC_NAME;
		return (NetcdfService) Naming.lookup(svcName);
	}

	/**
	 * Given a NetcdfRemoteProxy, construct a RemoteNetcdf.
	 * The NetcdfRemoteProxy would be obtained from a directory
	 * service like NetcdfService.
	 */
	public
	RemoteNetcdf(NetcdfRemoteProxy remote)
			throws IOException
	{
		super(remote.getSchema(), false);
		this.remote = remote;
		try {
			super.initHashtable();
		}
		catch (InstantiationException ie)
		{
			// Can't happen: Variable is concrete
			throw new Error();
		}
		catch (IllegalAccessException iae)
		{
			// Can't happen: Variable is accessable
			throw new Error();
		}
		catch (InvocationTargetException ite)
		{
			// all the possible target exceptions are
			// RuntimeException
			throw (RuntimeException)
				ite.getTargetException();
		}
	}

	/**
	 * Open up a remote Netcdf by name.
	 * The remote host needs to be running a NetcdfService
	 * which exports the data set.
	 * @param remoteHostName String host name or dotted quad
	 * @param dataSetName String name of the remote Netcdf
	 */
	public
	RemoteNetcdf(String remoteHostName,
		String dataSetName)
			throws IOException,
				java.rmi.NotBoundException,
				java.net.MalformedURLException
	{
		this(getNetcdfService(remoteHostName).lookup(dataSetName));
	}

	protected Accessor
	ioFactory(ProtoVariable proto)
			throws InvocationTargetException
	{
		try {
			return remote.getAccessor(proto.getName());
		}
		catch (IOException ee)
		{
			throw new InvocationTargetException(ee);
		}
	}
	
	private /* final */ NetcdfRemoteProxy remote;

	public static void
	main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("Usage: TestNetcdfService nc_name");
			System.exit(1);
		}
		final String name = args[0];
		// else
		try {
			RemoteNetcdf rnc = new RemoteNetcdf("localhost", name);
			System.out.println(rnc);
			VariableIterator vi = rnc.iterator();
			while(vi.hasNext())
			{
				Variable v = vi.next();
				System.out.print(v.getName() + "[0, ...]: ");
				MultiArray ma = v.copyout(new int[v.getRank()],
					v.getLengths());
				System.out.println(ma.get(
					new int[ma.getRank()]));
			}
		}
		catch (Exception ee)
		{
			System.out.println(ee);
			System.exit(1);
		}
		System.exit(0);
	}
}
