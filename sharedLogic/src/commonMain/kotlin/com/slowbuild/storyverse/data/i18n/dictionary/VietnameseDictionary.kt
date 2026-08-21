package com.slowbuild.storyverse.data.i18n.dictionary

import com.slowbuild.storyverse.domain.i18n.AppStringKey

val VietnameseDictionary: Map<AppStringKey, String> = mapOf(
    // App Shell & Navigation
    AppStringKey.APP_NAME to "StoryVerse",
    AppStringKey.TAB_DISCOVER to "Khám Phá",
    AppStringKey.TAB_SEARCH to "Tìm Kiếm",
    AppStringKey.TAB_LIBRARY to "Tủ Sách",
    AppStringKey.TAB_SETTINGS to "Cài Đặt",
    AppStringKey.TAB_AI_STUDIO to "AI Sáng Tác",

    // Home & Discovery Sections
    AppStringKey.SECTION_FEATURED to "Truyện Nổi Bật",
    AppStringKey.SECTION_POPULAR to "Đọc Nhiều Nhất",
    AppStringKey.SECTION_LATEST to "Mới Cập Nhật",
    AppStringKey.SECTION_COMPLETED to "Truyện Hoàn Thành",
    AppStringKey.SECTION_RECOMMENDED to "Gợi Ý Cho Bạn",
    AppStringKey.VIEW_MORE to "Xem thêm",
    AppStringKey.HOT_STORIES to "Truyện Hot",
    AppStringKey.DISCOVER_TITLE to "Khám Phá Thế Giới Truyện",

    // Search & Filter
    AppStringKey.SEARCH_HINT to "Tìm tên truyện, tác giả...",
    AppStringKey.SEARCH_NO_RESULTS to "Không tìm thấy truyện nào phù hợp",
    AppStringKey.SEARCH_RECENT_TITLE to "Tìm kiếm gần đây",
    AppStringKey.SEARCH_CLEAR_HISTORY to "Xóa lịch sử",
    AppStringKey.FILTER_ALL to "Tất cả",
    AppStringKey.FILTER_STATUS to "Trạng thái",
    AppStringKey.FILTER_CATEGORY to "Thể loại",
    AppStringKey.FILTER_SORT_BY to "Sắp xếp theo",
    AppStringKey.SORT_POPULAR to "Phổ biến nhất",
    AppStringKey.SORT_LATEST to "Mới nhất",
    AppStringKey.SORT_CHAPTERS to "Số chương",
    AppStringKey.SORT_RATING to "Đánh giá",

    // Story Statuses
    AppStringKey.STATUS_ONGOING to "Đang ra",
    AppStringKey.STATUS_COMPLETED to "Hoàn thành",
    AppStringKey.STATUS_HIATUS to "Tạm dừng",
    AppStringKey.STATUS_UNKNOWN to "Chưa rõ",

    // Story Detail
    AppStringKey.DETAIL_AUTHOR to "Tác giả",
    AppStringKey.DETAIL_CATEGORY to "Thể loại",
    AppStringKey.DETAIL_STATUS to "Trạng thái",
    AppStringKey.DETAIL_CHAPTERS to "chương",
    AppStringKey.DETAIL_RATING to "Đánh giá",
    AppStringKey.DETAIL_VIEWS to "lượt xem",
    AppStringKey.DETAIL_READ_NOW to "Đọc từ đầu",
    AppStringKey.DETAIL_CONTINUE_READING to "Đọc tiếp",
    AppStringKey.DETAIL_ADD_LIBRARY to "Thêm vào tủ sách",
    AppStringKey.DETAIL_IN_LIBRARY to "Đã trong tủ sách",
    AppStringKey.DETAIL_DOWNLOAD to "Tải truyện",
    AppStringKey.DETAIL_DOWNLOADED to "Đã tải offline",
    AppStringKey.DETAIL_DOWNLOADING to "Đang tải {0}%",
    AppStringKey.DETAIL_DOWNLOAD_SUCCESS to "Tải truyện thành công!",
    AppStringKey.DETAIL_DOWNLOAD_FAILED to "Tải thất bại: {0}",
    AppStringKey.DETAIL_READING_NOW to "Đang đọc",
    AppStringKey.DETAIL_DESCRIPTION_TITLE to "Giới Thiệu Nội Dung",
    AppStringKey.DETAIL_CHAPTER_LIST_TITLE to "Danh Sách Chương",
    AppStringKey.DETAIL_LATEST_CHAPTER_FORMAT to "Chương mới nhất: {0}",

    // Reader
    AppStringKey.READER_CHAPTER_INDEX_FORMAT to "Chương {0}: {1}",
    AppStringKey.READER_NEXT_CHAPTER to "Chương sau",
    AppStringKey.READER_PREV_CHAPTER to "Chương trước",
    AppStringKey.READER_FONT_SIZE to "Cỡ chữ",
    AppStringKey.READER_LINE_SPACING to "Giãn dòng",
    AppStringKey.READER_THEME to "Chế độ đọc",
    AppStringKey.READER_BOOKMARK_ADDED to "Đã thêm dấu trang",
    AppStringKey.READER_BOOKMARK_REMOVED to "Đã xóa dấu trang",
    AppStringKey.READER_CHAPTER_LOADING to "Đang tải nội dung chương...",
    AppStringKey.READER_CHAPTER_LOADING_FAILED to "Không thể tải chương. Nhấn để thử lại.",
    AppStringKey.READER_TOC_TITLE to "Mục Lục",
    AppStringKey.READER_PROGRESS_FORMAT to "Đang đọc {0}%",

    // Library & History
    AppStringKey.LIBRARY_TITLE to "Tủ Sách Cá Nhân",
    AppStringKey.LIBRARY_EMPTY_TITLE to "Tủ sách đang trống",
    AppStringKey.LIBRARY_EMPTY_SUBTITLE to "Hãy khám phá và lưu những bộ truyện yêu thích vào đây!",
    AppStringKey.LIBRARY_TAB_FAVORITES to "Yêu thích",
    AppStringKey.LIBRARY_TAB_HISTORY to "Lịch sử đọc",
    AppStringKey.LIBRARY_TAB_DOWNLOADS to "Đã tải về",
    AppStringKey.LIBRARY_TAB_LOCAL_FILES to "Tệp EPUB",
    AppStringKey.LIBRARY_CLEAR_HISTORY_CONFIRM to "Bạn có chắc chắn muốn xóa toàn bộ lịch sử đọc?",

    // Settings
    AppStringKey.SETTINGS_TITLE to "Cài Đặt",
    AppStringKey.SETTINGS_SECTION_APPEARANCE to "Giao Diện & Hiển Thị",
    AppStringKey.SETTINGS_THEME_TITLE to "Chủ đề giao diện",
    AppStringKey.SETTINGS_SECTION_LANGUAGE to "Ngôn Ngữ (Language)",
    AppStringKey.SETTINGS_LANGUAGE_TITLE to "Ngôn ngữ ứng dụng",
    AppStringKey.SETTINGS_SECTION_STORAGE to "Bộ Nhớ & Dữ Liệu",
    AppStringKey.SETTINGS_STORAGE_USAGE to "Dung lượng đã dùng",
    AppStringKey.SETTINGS_ACTIVE_SOURCE to "Nguồn truyện hoạt động: {0}",
    AppStringKey.SETTINGS_CLEAR_CACHE to "Xóa bộ nhớ đệm",
    AppStringKey.SETTINGS_CLEAR_CACHE_SUCCESS to "Đã xóa sạch bộ nhớ đệm",
    AppStringKey.SETTINGS_SECTION_ABOUT to "Thông Tin Ứng Dụng",
    AppStringKey.SETTINGS_VERSION_FORMAT to "Phiên bản {0}",
    AppStringKey.SETTINGS_DEVELOPER to "Phát triển bởi SlowBuild Team",

    // Common Actions & Dialogs
    AppStringKey.ACTION_CONFIRM to "Xác nhận",
    AppStringKey.ACTION_CANCEL to "Hủy",
    AppStringKey.ACTION_RETRY to "Thử lại",
    AppStringKey.ACTION_DELETE to "Xóa",
    AppStringKey.ACTION_SAVE to "Lưu",
    AppStringKey.ACTION_CLOSE to "Đóng",
    AppStringKey.ACTION_BACK to "Quay lại",
    AppStringKey.ACTION_SHARE to "Chia sẻ",
    AppStringKey.ACTION_REFRESH to "Làm mới",

    // Error Messages
    AppStringKey.ERROR_NETWORK to "Không có kết nối mạng. Vui lòng kiểm tra lại.",
    AppStringKey.ERROR_UNKNOWN to "Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.",
    AppStringKey.ERROR_NOT_FOUND to "Không tìm thấy dữ liệu yêu cầu.",
    AppStringKey.ERROR_TIMEOUT to "Kết nối quá thời gian chờ. Vui lòng thử lại.",
    AppStringKey.ERROR_SOURCE_UNAVAILABLE to "Nguồn truyện hiện không khả dụng."
)
