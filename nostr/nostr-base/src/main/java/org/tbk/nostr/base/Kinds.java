package org.tbk.nostr.base;

// from https://github.com/nbd-wtf/go-nostr/blob/master/kinds.go
public class Kinds {
    public static Kind kindProfileMetadata = Kind.of(0);
    public static Kind kindTextNote = Kind.of(1);
    public static Kind kindRecommendServer = Kind.of(2);
    public static Kind kindFollowList = Kind.of(3);

    @Deprecated
    public static Kind kindEncryptedDirectMessage = Kind.of(4);
    public static Kind kindDeletion = Kind.of(5);
    public static Kind kindRepost = Kind.of(6);
    public static Kind kindReaction = Kind.of(7);
    public static Kind kindBadgeAward = Kind.of(8);
    public static Kind kindSimpleGroupChatMessage = Kind.of(9);
    public static Kind kindSimpleGroupThreadedReply = Kind.of(10);
    public static Kind kindSimpleGroupThread = Kind.of(11);
    public static Kind kindSimpleGroupReply = Kind.of(12);
    public static Kind kindSeal = Kind.of(13);
    public static Kind kindDirectMessage = Kind.of(14);
    public static Kind kindGenericRepost = Kind.of(16);
    public static Kind kindReactionToWebsite = Kind.of(17);
    public static Kind kindChannelCreation = Kind.of(40);
    public static Kind kindChannelMetadata = Kind.of(41);
    public static Kind kindChannelMessage = Kind.of(42);
    public static Kind kindChannelHideMessage = Kind.of(43);
    public static Kind kindChannelMuteUser = Kind.of(44);
    public static Kind kindChess = Kind.of(64);
    public static Kind kindMergeRequests = Kind.of(818);
    public static Kind kindBid = Kind.of(1021);
    public static Kind kindBidConfirmation = Kind.of(1022);
    public static Kind kindOpenTimestamps = Kind.of(1040);
    public static Kind kindGiftWrap = Kind.of(1059);
    public static Kind kindFileMetadata = Kind.of(1063);
    public static Kind kindLiveChatMessage = Kind.of(1311);
    public static Kind kindPatch = Kind.of(1617);
    public static Kind kindIssue = Kind.of(1621);
    public static Kind kindReply = Kind.of(1622);
    public static Kind kindStatusOpen = Kind.of(1630);
    public static Kind kindStatusApplied = Kind.of(1631);
    public static Kind kindStatusClosed = Kind.of(1632);
    public static Kind kindStatusDraft = Kind.of(1633);
    public static Kind kindProblemTracker = Kind.of(1971);
    public static Kind kindReporting = Kind.of(1984);
    public static Kind kindLabel = Kind.of(1985);
    public static Kind kindRelayReviews = Kind.of(1986);
    public static Kind kindAIEmbeddings = Kind.of(1987);
    public static Kind kindTorrent = Kind.of(2003);
    public static Kind kindTorrentComment = Kind.of(2004);
    public static Kind kindCoinjoinPool = Kind.of(2022);
    public static Kind kindCommunityPostApproval = Kind.of(4550);
    public static Kind kindJobFeedback = Kind.of(7000);
    public static Kind kindSimpleGroupPutUser = Kind.of(9000);
    public static Kind kindSimpleGroupRemoveUser = Kind.of(9001);
    public static Kind kindSimpleGroupEditMetadata = Kind.of(9002);
    public static Kind kindSimpleGroupDeleteEvent = Kind.of(9005);
    public static Kind kindSimpleGroupCreateGroup = Kind.of(9007);
    public static Kind kindSimpleGroupDeleteGroup = Kind.of(9008);
    public static Kind kindSimpleGroupCreateInvite = Kind.of(9009);
    public static Kind kindSimpleGroupJoinRequest = Kind.of(9021);
    public static Kind kindSimpleGroupLeaveRequest = Kind.of(9022);
    public static Kind kindZapGoal = Kind.of(9041);
    public static Kind kindTidalLogin = Kind.of(9467);
    public static Kind kindZapRequest = Kind.of(9734);
    public static Kind kindZap = Kind.of(9735);
    public static Kind kindHighlights = Kind.of(9802);
    public static Kind kindMuteList = Kind.of(10000);
    public static Kind kindPinList = Kind.of(10001);
    public static Kind kindRelayListMetadata = Kind.of(10002);
    public static Kind kindBookmarkList = Kind.of(10003);
    public static Kind kindCommunityList = Kind.of(10004);
    public static Kind kindPublicChatList = Kind.of(10005);
    public static Kind kindBlockedRelayList = Kind.of(10006);
    public static Kind kindSearchRelayList = Kind.of(10007);
    public static Kind kindSimpleGroupList = Kind.of(10009);
    public static Kind kindInterestList = Kind.of(10015);
    public static Kind kindEmojiList = Kind.of(10030);
    public static Kind kindDMRelayList = Kind.of(10050);
    public static Kind kindUserServerList = Kind.of(10063);
    public static Kind kindFileStorageServerList = Kind.of(10096);
    public static Kind kindGoodWikiAuthorList = Kind.of(10101);
    public static Kind kindGoodWikiRelayList = Kind.of(10102);
    public static Kind kindNWCWalletInfo = Kind.of(13194);
    public static Kind kindLightningPubRPC = Kind.of(21000);
    public static Kind kindClientAuthentication = Kind.of(22242);
    public static Kind kindNWCWalletRequest = Kind.of(23194);
    public static Kind kindNWCWalletResponse = Kind.of(23195);
    public static Kind kindNostrConnect = Kind.of(24133);
    public static Kind kindBlobs = Kind.of(24242);
    public static Kind kindHTTPAuth = Kind.of(27235);
    public static Kind kindCategorizedPeopleList = Kind.of(30000);
    public static Kind kindCategorizedBookmarksList = Kind.of(30001);
    public static Kind kindRelaySets = Kind.of(30002);
    public static Kind kindBookmarkSets = Kind.of(30003);
    public static Kind kindCuratedSets = Kind.of(30004);
    public static Kind kindCuratedVideoSets = Kind.of(30005);
    public static Kind kindMuteSets = Kind.of(30007);
    public static Kind kindProfileBadges = Kind.of(30008);
    public static Kind kindBadgeDefinition = Kind.of(30009);
    public static Kind kindInterestSets = Kind.of(30015);
    public static Kind kindStallDefinition = Kind.of(30017);
    public static Kind kindProductDefinition = Kind.of(30018);
    public static Kind kindMarketplaceUI = Kind.of(30019);
    public static Kind kindProductSoldAsAuction = Kind.of(30020);
    public static Kind kindArticle = Kind.of(30023);
    public static Kind kindDraftArticle = Kind.of(30024);
    public static Kind kindEmojiSets = Kind.of(30030);
    public static Kind kindModularArticleHeader = Kind.of(30040);
    public static Kind kindModularArticleContent = Kind.of(30041);
    public static Kind kindReleaseArtifactSets = Kind.of(30063);
    public static Kind kindApplicationSpecificData = Kind.of(30078);
    public static Kind kindLiveEvent = Kind.of(30311);
    public static Kind kindUserStatuses = Kind.of(30315);
    public static Kind kindClassifiedListing = Kind.of(30402);
    public static Kind kindDraftClassifiedListing = Kind.of(30403);
    public static Kind kindRepositoryAnnouncement = Kind.of(30617);
    public static Kind kindRepositoryState = Kind.of(30618);
    public static Kind kindSimpleGroupMetadata = Kind.of(39000);
    public static Kind kindSimpleGroupAdmins = Kind.of(39001);
    public static Kind kindSimpleGroupMembers = Kind.of(39002);
    public static Kind kindSimpleGroupRoles = Kind.of(39003);
    public static Kind kindWikiArticle = Kind.of(30818);
    public static Kind kindRedirects = Kind.of(30819);
    public static Kind kindFeed = Kind.of(31890);
    public static Kind kindDateCalendarEvent = Kind.of(31922);
    public static Kind kindTimeCalendarEvent = Kind.of(31923);
    public static Kind kindCalendar = Kind.of(31924);
    public static Kind kindCalendarEventRSVP = Kind.of(31925);
    public static Kind kindHandlerRecommendation = Kind.of(31989);
    public static Kind kindHandlerInformation = Kind.of(31990);
    public static Kind kindVideoEvent = Kind.of(34235);
    public static Kind kindShortVideoEvent = Kind.of(34236);
    public static Kind kindVideoViewEvent = Kind.of(34237);
    public static Kind kindCommunityDefinition = Kind.of(34550);
}
