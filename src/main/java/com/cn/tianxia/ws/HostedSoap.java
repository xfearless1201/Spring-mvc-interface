/**
 * HostedSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.cn.tianxia.ws;

public interface HostedSoap extends java.rmi.Remote {

    /**
     * Get a list of Coupons/Bonuses available to a player for redemption
     */
    public com.cn.tianxia.ws.CouponInfoDTO[] getBonusAvailablePlayer(com.cn.tianxia.ws.BonusAvailablePlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Apply a bonus coupon to a player
     */
    public com.cn.tianxia.ws.CouponResponseMessage applyBonusToPlayer(com.cn.tianxia.ws.ApplyBonusToPlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of the players bonus balances (these are coupons
     * which have been activated on players account and are ready to use)
     */
    public com.cn.tianxia.ws.BonusBalancesDTO[] getBonusBalancesForPlayer(com.cn.tianxia.ws.BonusGenericPlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Toggle the provided BonusBalanceId active status.
     */
    public com.cn.tianxia.ws.ToggleBonusBalanceResponse setPlayerBonusBalanceActive(com.cn.tianxia.ws.SetBonusBalanceActiveRequest req) throws java.rmi.RemoteException;

    /**
     * Deletes a Bonus Balance
     */
    public com.cn.tianxia.ws.ToggleBonusBalanceResponse deletePlayerBonusBalance(com.cn.tianxia.ws.DeleteBonusBalanceRequest req) throws java.rmi.RemoteException;

    /**
     * Create a new Coupon and optionally redeeme it for specified
     * username
     */
    public com.cn.tianxia.ws.CreateAndApplyBonusResponse createAndApplyBonus(com.cn.tianxia.ws.CreateBonusAndApplyRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of Game Types
     */
    public com.cn.tianxia.ws.GameTypeResponse getGameTypes(com.cn.tianxia.ws.GameTypeRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of all available Games for the Brand.
     */
    public com.cn.tianxia.ws.GameResponse getGames(com.cn.tianxia.ws.GameRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of all available Games for the Brand but only if
     * they appear in the Backoffice Game Menu Display [same format as GetGames()]
     */
    public com.cn.tianxia.ws.GameResponse getGamesInMenuOnly(com.cn.tianxia.ws.GameRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of all Games for the Brand in hierarchical menu
     * format structure as per Backoffice Game Menu Display
     */
    public com.cn.tianxia.ws.GameDisplayResponse getGameDisplay(com.cn.tianxia.ws.GameDisplayRequest req) throws java.rmi.RemoteException;

    /**
     * Get a detailed list of all Jackpots in the Brand
     */
    public com.cn.tianxia.ws.JackpotInfoDTO[] getJackpots(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of all Jackpots id's in the Brand with a list of
     * linked BrandGameIds. This shows you which Jackpots a specific Game
     * is using.
     */
    public com.cn.tianxia.ws.JackpotGameLinkInfoDTO[] getJackpotGameLink(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException;

    /**
     * Get a detailed list of all Jackpots in all the Brands in same
     * Group]
     */
    public com.cn.tianxia.ws.JackpotInfoDTO[] getAllJackpotsInAllBrands(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException;

    /**
     * Get a list of all Jackpots id's for all Brands in a Group with
     * a list of linked BrandGameIds
     */
    public com.cn.tianxia.ws.JackpotGameLinkInfoDTO[] getJackpotGameLinkInAllBrands(com.cn.tianxia.ws.JackpotInfoRequest req) throws java.rmi.RemoteException;

    /**
     * Get the summarised Total stakes and payouts per player for
     * a Brand during the date range. Winners/Losers report. ONLY completed
     * games. Hours granularity
     */
    public com.cn.tianxia.ws.PlayerStakePayoutDTO[] reportPlayerStakePayout(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get individual debit and credit transactions per game for a
     * player in a date range. Seconds granularity.
     */
    public com.cn.tianxia.ws.PlayerGameTransactionsDTO[] getPlayerGameTransactions(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get individual money transfers in and out for a player in a
     * date range. Same result format as GetBrandTransferTransactions().
     * Seconds granularity.
     */
    public com.cn.tianxia.ws.PlayerTransferTransactionsDTO[] getPlayerTransferTransactions(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get all individual money transfers in and out for the brand
     * in a date range. Same result format as GetPlayerTransferTransactions().
     * Seconds granularity.
     */
    public com.cn.tianxia.ws.PlayerTransferTransactionsDTO[] getBrandTransferTransactions(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get all individual money transfers in and out for the GROUP
     * in a date range. Same result format as GetPlayerTransferTransactions().
     * Seconds granularity.
     */
    public com.cn.tianxia.ws.PlayerTransferTransactionsDTO[] getGroupTransferTransactions(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get individual game instance results for a player in a date
     * range. INCLUDES incomplete games. Seconds granularity
     */
    public com.cn.tianxia.ws.PlayerGameResultsDTO[] getPlayerGameResults(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException;

    /**
     * NOTE: Please Use GetBrandCompletedGameResults(). Get up to
     * 10000 records, up to 7 days ago, of individual game instance results
     * for players in a brand in the date range. Includes incomplete games.
     * Seconds granularity
     */
    public com.cn.tianxia.ws.PlayerGameResultsDTO[] getBrandGameResults(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get completed game instance results for players in a brand
     * where the Completed Date of the game is in the date range. Seconds
     * granularity
     */
    public com.cn.tianxia.ws.PlayerCompletedGamesDTO[] getBrandCompletedGameResults(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get completed game instance results for players in GROUP Wide
     * where the Completed Date of the game is in the date range. The Group
     * will be determined by the brandId requested. Seconds granularity
     */
    public com.cn.tianxia.ws.PlayerCompletedGamesDTO[] getGroupCompletedGameResults(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get a single players summed Stake, Payout, Jackpot win (portion
     * of the Payout), Jackpot Contributions in 1 row. INCLUDES incomplete
     * games. Seconds granularity
     */
    public com.cn.tianxia.ws.PlayerStakePayoutSummaryDTO getPlayerStakePayoutSummary(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get overall contributions report for each Jackpot in a Brand
     * in the individual funding currency. Hours granularity
     */
    public com.cn.tianxia.ws.JackpotContributionRecord[] reportJackpotContribution(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get overall contributions per Game for each Jackpot in a Brand.
     * Hours granularity
     */
    public com.cn.tianxia.ws.JackpotContributionPerGameRecord[] reportJackpotContributionPerGame(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get the results from a Dynamic Report configured in the Back
     * Office
     */
    public com.cn.tianxia.ws.ReportDynamicResponseReportDynamicResult reportDynamic(com.cn.tianxia.ws.DynamicReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get game overview/summary report for each Game in a Brand.
     * ONLY completed games. Hour granularity.
     */
    public com.cn.tianxia.ws.GameOverviewRecord[] reportGameOverviewBrand(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get game overview/summary report for games played by Player
     * in date range. ONLY completed games. Hour granularity.
     */
    public com.cn.tianxia.ws.PlayerGameOverviewRecord[] reportGameOverviewPlayer(com.cn.tianxia.ws.PlayerReportRequest req) throws java.rmi.RemoteException;

    /**
     * Get game overview report for each Game in a POS/Kiosk Location
     * in a Brand [For Terminals/Kiosk usage only], Hour granularity.
     */
    public com.cn.tianxia.ws.GameOverviewPerLocationRecord[] reportGameOverviewPerLocation(com.cn.tianxia.ws.ReportRequest req) throws java.rmi.RemoteException;

    /**
     * Update player password.
     */
    public com.cn.tianxia.ws.UpdatePlayerPasswordResponse updatePlayerPassword(com.cn.tianxia.ws.UpdatePlayerPasswordRequest req) throws java.rmi.RemoteException;

    /**
     * Logs in player and creates/updates Player using provided details.
     * On creation the wallet is created using the currency code provided.
     * This cannot be changed. Returns the Session Token used to launch game.
     */
    public com.cn.tianxia.ws.LoginUserResponse loginOrCreatePlayer(com.cn.tianxia.ws.LoginOrCreatePlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Query a Deposit or Withdraw RequestId to get the status
     */
    public com.cn.tianxia.ws.QueryTransferResponse queryTransfer(com.cn.tianxia.ws.QueryTransferRequest req) throws java.rmi.RemoteException;

    /**
     * Query player record for current balance
     */
    public com.cn.tianxia.ws.QueryPlayerResponse queryPlayer(com.cn.tianxia.ws.QueryPlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Logout a Habanero Wallet Player (Note this will not work for
     * Single/Seamless wallet)
     */
    public com.cn.tianxia.ws.LogoutPlayerResponse logoutPlayer(com.cn.tianxia.ws.LogoutPlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Logout a Player using external token for single wallet.
     */
    public com.cn.tianxia.ws.ThirdPartyPlayerLogoutResponse logoutThirdPartyPlayer(com.cn.tianxia.ws.LogoutThirdPartyPlayerRequest req) throws java.rmi.RemoteException;

    /**
     * Deposit money into player wallet. If player exists the currency
     * code must match. Otherwise a new player will be created.
     */
    public com.cn.tianxia.ws.MoneyResponse depositPlayerMoney(com.cn.tianxia.ws.DepositPlayerMoneyRequest req) throws java.rmi.RemoteException;

    /**
     * Withdraw money from player wallet.
     */
    public com.cn.tianxia.ws.MoneyResponse withdrawPlayerMoney(com.cn.tianxia.ws.WithdrawPlayerMoneyRequest req) throws java.rmi.RemoteException;

    /**
     * Logouts out all Thirdparty players in a Brand.
     */
    public com.cn.tianxia.ws.LogoutAllPlayersInBrandResponse logoutAllPlayersInBrand(com.cn.tianxia.ws.LogoutAllPlayersInBrandRequest req) throws java.rmi.RemoteException;

    /**
     * Set Maintenance mode ON or OFF for a Group.
     */
    public com.cn.tianxia.ws.MaintenanceModeResponse setMaintenanceMode(com.cn.tianxia.ws.MaintenanceModeRequest req) throws java.rmi.RemoteException;
}
