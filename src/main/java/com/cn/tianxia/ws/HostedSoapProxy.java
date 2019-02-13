package com.cn.tianxia.ws;

public class HostedSoapProxy implements com.cn.tianxia.ws.HostedSoap {
  private String _endpoint = null;
  private com.cn.tianxia.ws.HostedSoap hostedSoap = null;
  
  public HostedSoapProxy() {
    _initHostedSoapProxy();
  }
  
  public HostedSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initHostedSoapProxy();
  }
  
  private void _initHostedSoapProxy() {
    try {
      hostedSoap = (new com.cn.tianxia.ws.HostedLocator()).getHostedSoap();
      if (hostedSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)hostedSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)hostedSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (hostedSoap != null)
      ((javax.xml.rpc.Stub)hostedSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.cn.tianxia.ws.HostedSoap getHostedSoap() {
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap;
  }
  
  public com.cn.tianxia.ws.CouponInfoDTO[] getBonusAvailablePlayer(com.cn.tianxia.ws.BonusAvailablePlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getBonusAvailablePlayer(req);
  }
  
  public com.cn.tianxia.ws.CouponResponseMessage applyBonusToPlayer(com.cn.tianxia.ws.ApplyBonusToPlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.applyBonusToPlayer(req);
  }
  
  public com.cn.tianxia.ws.BonusBalancesDTO[] getBonusBalancesForPlayer(com.cn.tianxia.ws.BonusGenericPlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getBonusBalancesForPlayer(req);
  }
  
  public com.cn.tianxia.ws.ToggleBonusBalanceResponse setPlayerBonusBalanceActive(com.cn.tianxia.ws.SetBonusBalanceActiveRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.setPlayerBonusBalanceActive(req);
  }
  
  public com.cn.tianxia.ws.ToggleBonusBalanceResponse deletePlayerBonusBalance(com.cn.tianxia.ws.DeleteBonusBalanceRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.deletePlayerBonusBalance(req);
  }
  
  public com.cn.tianxia.ws.CreateAndApplyBonusResponse createAndApplyBonus(com.cn.tianxia.ws.CreateBonusAndApplyRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.createAndApplyBonus(req);
  }
  
  public com.cn.tianxia.ws.GameTypeResponse getGameTypes(com.cn.tianxia.ws.GameTypeRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getGameTypes(req);
  }
  
  public com.cn.tianxia.ws.GameResponse getGames(com.cn.tianxia.ws.GameRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getGames(req);
  }
  
  public com.cn.tianxia.ws.GameResponse getGamesInMenuOnly(com.cn.tianxia.ws.GameRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getGamesInMenuOnly(req);
  }
  
  public com.cn.tianxia.ws.GameDisplayResponse getGameDisplay(com.cn.tianxia.ws.GameDisplayRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getGameDisplay(req);
  }
  
  public com.cn.tianxia.ws.JackpotInfoDTO[] getJackpots(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getJackpots(req);
  }
  
  public com.cn.tianxia.ws.JackpotGameLinkInfoDTO[] getJackpotGameLink(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getJackpotGameLink(req);
  }
  
  public com.cn.tianxia.ws.JackpotInfoDTO[] getAllJackpotsInAllBrands(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getAllJackpotsInAllBrands(req);
  }
  
  public com.cn.tianxia.ws.JackpotGameLinkInfoDTO[] getJackpotGameLinkInAllBrands(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getJackpotGameLinkInAllBrands(req);
  }
  
  public com.cn.tianxia.ws.PlayerStakePayoutDTO[] reportPlayerStakePayout(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportPlayerStakePayout(req);
  }
  
  public com.cn.tianxia.ws.PlayerGameTransactionsDTO[] getPlayerGameTransactions(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getPlayerGameTransactions(req);
  }
  
  public com.cn.tianxia.ws.PlayerTransferTransactionsDTO[] getPlayerTransferTransactions(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getPlayerTransferTransactions(req);
  }
  
  public com.cn.tianxia.ws.PlayerTransferTransactionsDTO[] getBrandTransferTransactions(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getBrandTransferTransactions(req);
  }
  
  public com.cn.tianxia.ws.PlayerTransferTransactionsDTO[] getGroupTransferTransactions(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getGroupTransferTransactions(req);
  }
  
  public com.cn.tianxia.ws.PlayerGameResultsDTO[] getPlayerGameResults(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getPlayerGameResults(req);
  }
  
  public com.cn.tianxia.ws.PlayerGameResultsDTO[] getBrandGameResults(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getBrandGameResults(req);
  }
  
  public com.cn.tianxia.ws.PlayerCompletedGamesDTO[] getBrandCompletedGameResults(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getBrandCompletedGameResults(req);
  }
  
  public com.cn.tianxia.ws.PlayerCompletedGamesDTO[] getGroupCompletedGameResults(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getGroupCompletedGameResults(req);
  }
  
  public com.cn.tianxia.ws.PlayerStakePayoutSummaryDTO getPlayerStakePayoutSummary(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.getPlayerStakePayoutSummary(req);
  }
  
  public com.cn.tianxia.ws.JackpotContributionRecord[] reportJackpotContribution(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportJackpotContribution(req);
  }
  
  public com.cn.tianxia.ws.JackpotContributionPerGameRecord[] reportJackpotContributionPerGame(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportJackpotContributionPerGame(req);
  }
  
  public com.cn.tianxia.ws.ReportDynamicResponseReportDynamicResult reportDynamic(com.cn.tianxia.ws.DynamicReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportDynamic(req);
  }
  
  public com.cn.tianxia.ws.GameOverviewRecord[] reportGameOverviewBrand(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportGameOverviewBrand(req);
  }
  
  public com.cn.tianxia.ws.PlayerGameOverviewRecord[] reportGameOverviewPlayer(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportGameOverviewPlayer(req);
  }
  
  public com.cn.tianxia.ws.GameOverviewPerLocationRecord[] reportGameOverviewPerLocation(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.reportGameOverviewPerLocation(req);
  }
  
  public com.cn.tianxia.ws.UpdatePlayerPasswordResponse updatePlayerPassword(com.cn.tianxia.ws.UpdatePlayerPasswordRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.updatePlayerPassword(req);
  }
  
  public com.cn.tianxia.ws.LoginUserResponse loginOrCreatePlayer(com.cn.tianxia.ws.LoginOrCreatePlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.loginOrCreatePlayer(req);
  }
  
  public com.cn.tianxia.ws.QueryTransferResponse queryTransfer(com.cn.tianxia.ws.QueryTransferRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.queryTransfer(req);
  }
  
  public com.cn.tianxia.ws.QueryPlayerResponse queryPlayer(com.cn.tianxia.ws.QueryPlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.queryPlayer(req);
  }
  
  public com.cn.tianxia.ws.LogoutPlayerResponse logoutPlayer(com.cn.tianxia.ws.LogoutPlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.logoutPlayer(req);
  }
  
  public com.cn.tianxia.ws.ThirdPartyPlayerLogoutResponse logoutThirdPartyPlayer(com.cn.tianxia.ws.LogoutThirdPartyPlayerRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.logoutThirdPartyPlayer(req);
  }
  
  public com.cn.tianxia.ws.MoneyResponse depositPlayerMoney(com.cn.tianxia.ws.DepositPlayerMoneyRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.depositPlayerMoney(req);
  }
  
  public com.cn.tianxia.ws.MoneyResponse withdrawPlayerMoney(com.cn.tianxia.ws.WithdrawPlayerMoneyRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.withdrawPlayerMoney(req);
  }
  
  public com.cn.tianxia.ws.LogoutAllPlayersInBrandResponse logoutAllPlayersInBrand(com.cn.tianxia.ws.LogoutAllPlayersInBrandRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.logoutAllPlayersInBrand(req);
  }
  
  public com.cn.tianxia.ws.MaintenanceModeResponse setMaintenanceMode(com.cn.tianxia.ws.MaintenanceModeRequest req) throws java.rmi.RemoteException{
    if (hostedSoap == null)
      _initHostedSoapProxy();
    return hostedSoap.setMaintenanceMode(req);
  }
  
  
}