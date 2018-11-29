package aos;

public class AddServiceProxy implements aos.AddService {
  private String _endpoint = null;
  private aos.AddService addService = null;
  
  public AddServiceProxy() {
    _initAddServiceProxy();
  }
  
  public AddServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initAddServiceProxy();
  }
  
  private void _initAddServiceProxy() {
    try {
      addService = (new aos.AddServiceServiceLocator()).getAddService();
      if (addService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)addService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)addService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (addService != null)
      ((javax.xml.rpc.Stub)addService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public aos.AddService getAddService() {
    if (addService == null)
      _initAddServiceProxy();
    return addService;
  }
  
  public int add() throws java.rmi.RemoteException{
    if (addService == null)
      _initAddServiceProxy();
    return addService.add();
  }
  
  public double myload() throws java.rmi.RemoteException{
    if (addService == null)
      _initAddServiceProxy();
    return addService.myload();
  }
  
  
}