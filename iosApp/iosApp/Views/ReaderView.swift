import SwiftUI
import SharedLogic

public struct ReaderView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = ReaderViewModel()

    public let storyId: String
    @State public var chapterId: String

    public init(storyId: String, chapterId: String) {
        self.storyId = storyId
        self._chapterId = State(initialValue: chapterId)
    }

    private var currentColors: (bg: Color, text: Color) {
        switch viewModel.themePreset {
        case .light:
            return (Color.white, Color(hex: "1A1A1A"))
        case .sepia:
            return (Color(hex: "FBF0D9"), Color(hex: "5F4B32"))
        case .dark:
            return (Color(hex: "1E1E24"), Color(hex: "E0E0E0"))
        case .black:
            return (Color.black, Color(hex: "A0A0A0"))
        default:
            return (themeManager.colors.readerBackground, themeManager.colors.readerTextColor)
        }
    }

    private var currentFontDesign: Font.Design {
        switch viewModel.fontFamily {
        case .serif: return .serif
        case .monospace: return .monospaced
        case .sansSerif: return .default
        default: return .default
        }
    }

    public var body: some View {
        ZStack {
            // Main Reader Background
            currentColors.bg
                .ignoresSafeArea()

            // Content
            if viewModel.isLoading && viewModel.currentChapter == nil {
                LoadingView()
            } else if let error = viewModel.errorMessage, viewModel.currentChapter == nil {
                ErrorView(message: error) {
                    viewModel.loadChapter(storyId: storyId, chapterId: chapterId)
                }
            } else if let chapter = viewModel.currentChapter {
                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        // Chapter Header
                        VStack(spacing: 8) {
                            Text(chapter.title)
                                .font(.system(size: viewModel.fontSize + 5, weight: .bold, design: currentFontDesign))
                                .foregroundColor(themeManager.colors.primary)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)

                            HStack(spacing: 8) {
                                if viewModel.wordCount > 0 {
                                    Text("\(viewModel.wordCount) từ")
                                        .font(.system(size: 12))
                                        .foregroundColor(currentColors.text.opacity(0.7))
                                    Text("•")
                                        .font(.system(size: 12))
                                        .foregroundColor(currentColors.text.opacity(0.4))
                                }
                                Text("~\(viewModel.estimatedReadMinutes) phút đọc")
                                    .font(.system(size: 12))
                                    .foregroundColor(currentColors.text.opacity(0.7))
                            }
                        }
                        .padding(.top, 16)
                        .padding(.bottom, 12)

                        // Paragraphs
                        if !chapter.paragraphs.isEmpty {
                            ForEach(Array(chapter.paragraphs.enumerated()), id: \.offset) { _, paragraph in
                                Text(paragraph)
                                    .font(.system(size: viewModel.fontSize, design: currentFontDesign))
                                    .foregroundColor(currentColors.text)
                                    .lineSpacing(viewModel.fontSize * (viewModel.lineSpacingMultiplier - 1.0) * 1.5)
                            }
                        } else {
                            Text(chapter.content)
                                .font(.system(size: viewModel.fontSize, design: currentFontDesign))
                                .foregroundColor(currentColors.text)
                                .lineSpacing(viewModel.fontSize * (viewModel.lineSpacingMultiplier - 1.0) * 1.5)
                        }

                        // Bottom chapter navigation buttons inside scroll
                        HStack {
                            if let prev = viewModel.prevChapter {
                                Button(action: {
                                    self.chapterId = prev.id
                                    viewModel.loadChapter(storyId: storyId, chapterId: prev.id)
                                }) {
                                    HStack {
                                        Image(systemName: "chevron.left")
                                        Text(localizedString(AppStringKey.readerPrevChapter))
                                    }
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(themeManager.colors.primary)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 8)
                                    .background(themeManager.colors.card)
                                    .cornerRadius(8)
                                }
                            }

                            Spacer()

                            if let next = viewModel.nextChapter {
                                Button(action: {
                                    self.chapterId = next.id
                                    viewModel.loadChapter(storyId: storyId, chapterId: next.id)
                                }) {
                                    HStack {
                                        Text(localizedString(AppStringKey.readerNextChapter))
                                        Image(systemName: "chevron.right")
                                    }
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(themeManager.colors.onPrimary)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 8)
                                    .background(themeManager.colors.primary)
                                    .cornerRadius(8)
                                }
                            }
                        }
                        .padding(.top, 24)
                        .padding(.bottom, 60)
                    }
                    .padding(.horizontal, viewModel.horizontalPadding)
                    .padding(.vertical, 20)
                }
                .onTapGesture {
                    viewModel.toggleControls()
                }
            }

            // Floating TopBar
            if viewModel.showControls {
                VStack {
                    HStack {
                        Button(action: { dismiss() }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(themeManager.colors.textPrimary)
                        }

                        Spacer()

                        Text(viewModel.currentChapter?.title ?? localizedString(AppStringKey.appName))
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(themeManager.colors.textPrimary)
                            .lineLimit(1)

                        Spacer()

                        Button(action: { viewModel.toggleBookmark() }) {
                            Image(systemName: viewModel.isBookmarked ? "bookmark.fill" : "bookmark")
                                .font(.system(size: 16))
                                .foregroundColor(viewModel.isBookmarked ? themeManager.colors.primary : themeManager.colors.textPrimary)
                        }

                        Button(action: { viewModel.toggleBookmarksSheet() }) {
                            Image(systemName: "bookmark.circle")
                                .font(.system(size: 16))
                                .foregroundColor(themeManager.colors.textPrimary)
                        }
                        .padding(.leading, 6)

                        Button(action: { viewModel.toggleTocSheet() }) {
                            Image(systemName: "list.bullet")
                                .font(.system(size: 16))
                                .foregroundColor(themeManager.colors.textPrimary)
                        }
                        .padding(.leading, 6)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 50)
                    .padding(.bottom, 12)
                    .background(themeManager.colors.background.opacity(0.95))
                    .overlay(
                        Divider().background(themeManager.colors.border),
                        alignment: .bottom
                    )

                    Spacer()
                }
                .ignoresSafeArea(edges: .top)
                .transition(.move(edge: .top).combined(with: .opacity))
            }

            // Floating BottomBar
            if viewModel.showControls {
                VStack {
                    Spacer()
                    HStack(spacing: 20) {
                        // Prev button
                        Button(action: {
                            if let prev = viewModel.prevChapter {
                                self.chapterId = prev.id
                                viewModel.loadChapter(storyId: storyId, chapterId: prev.id)
                            }
                        }) {
                            Image(systemName: "backward.fill")
                                .foregroundColor(viewModel.prevChapter != nil ? themeManager.colors.primary : themeManager.colors.textMuted)
                        }
                        .disabled(viewModel.prevChapter == nil)

                        // TOC button
                        Button(action: { viewModel.toggleTocSheet() }) {
                            Image(systemName: "list.bullet")
                                .foregroundColor(themeManager.colors.textPrimary)
                        }

                        Spacer()

                        // Reader Settings button
                        Button(action: { viewModel.toggleSettingsSheet() }) {
                            HStack(spacing: 4) {
                                Image(systemName: "textformat.size")
                                Text("\(Int(viewModel.fontSize)) pt")
                                    .font(.system(size: 12, weight: .bold))
                            }
                            .foregroundColor(themeManager.colors.primary)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(themeManager.colors.surfaceVariant)
                            .cornerRadius(8)
                        }

                        Spacer()

                        // Next button
                        Button(action: {
                            if let next = viewModel.nextChapter {
                                self.chapterId = next.id
                                viewModel.loadChapter(storyId: storyId, chapterId: next.id)
                            }
                        }) {
                            Image(systemName: "forward.fill")
                                .foregroundColor(viewModel.nextChapter != nil ? themeManager.colors.primary : themeManager.colors.textMuted)
                        }
                        .disabled(viewModel.nextChapter == nil)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 14)
                    .background(themeManager.colors.card)
                    .cornerRadius(16)
                    .shadow(color: Color.black.opacity(0.12), radius: 8, x: 0, y: -2)
                    .padding(.horizontal, 20)
                    .padding(.bottom, 16)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
        .onAppear {
            viewModel.loadChapter(storyId: storyId, chapterId: chapterId)
        }
        .sheet(isPresented: $viewModel.showTocSheet) {
            NavigationStack {
                List(viewModel.chapters, id: \.id) { chapter in
                    let isCurrent = chapter.id == self.chapterId
                    Button(action: {
                        self.chapterId = chapter.id
                        viewModel.showTocSheet = false
                        viewModel.loadChapter(storyId: storyId, chapterId: chapter.id)
                    }) {
                        HStack {
                            Text(chapter.title)
                                .font(.system(size: 15, weight: isCurrent ? .bold : .regular))
                                .foregroundColor(isCurrent ? themeManager.colors.primary : themeManager.colors.textPrimary)
                            Spacer()
                            if isCurrent {
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundColor(themeManager.colors.primary)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
                .navigationTitle("Mục Lục (\(viewModel.chapters.count) chương)")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Đóng") {
                            viewModel.showTocSheet = false
                        }
                    }
                }
            }
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $viewModel.showSettingsSheet) {
            ReaderSettingsSheet(viewModel: viewModel)
                .presentationDetents([.medium])
        }
        .sheet(isPresented: $viewModel.showBookmarksSheet) {
            NavigationStack {
                List {
                    if viewModel.bookmarks.isEmpty {
                        Text("Chưa có đánh dấu trang nào")
                            .font(.system(size: 14))
                            .foregroundColor(themeManager.colors.textSecondary)
                            .padding(.vertical, 20)
                    } else {
                        ForEach(viewModel.bookmarks, id: \.id) { bookmark in
                            Button(action: {
                                self.chapterId = bookmark.chapterId
                                viewModel.showBookmarksSheet = false
                                viewModel.loadChapter(storyId: storyId, chapterId: bookmark.chapterId)
                            }) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(bookmark.chapterTitle)
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(themeManager.colors.textPrimary)
                                    if let note = bookmark.note, !note.isEmpty {
                                        Text(note)
                                            .font(.system(size: 13))
                                            .foregroundColor(themeManager.colors.textSecondary)
                                    }
                                }
                                .padding(.vertical, 4)
                            }
                        }
                        .onDelete { indexSet in
                            for index in indexSet {
                                let b = viewModel.bookmarks[index]
                                viewModel.deleteBookmark(id: b.id)
                            }
                        }
                    }
                }
                .navigationTitle("Đánh Dấu Trang")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Đóng") {
                            viewModel.showBookmarksSheet = false
                        }
                    }
                }
            }
            .presentationDetents([.medium, .large])
        }
    }
}

struct ReaderSettingsSheet: View {
    @EnvironmentObject var themeManager: ThemeManager
    @ObservedObject var viewModel: ReaderViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                // Theme Presets
                VStack(alignment: .leading, spacing: 8) {
                    Text("Màu nền đọc")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(themeManager.colors.textSecondary)

                    HStack(spacing: 12) {
                        themeCircle(preset: .default_, bg: themeManager.colors.background, label: "Hệ thống")
                        themeCircle(preset: .light, bg: Color.white, label: "Sáng")
                        themeCircle(preset: .sepia, bg: Color(hex: "FBF0D9"), label: "Sepia")
                        themeCircle(preset: .dark, bg: Color(hex: "1E1E24"), label: "Tối")
                        themeCircle(preset: .black, bg: Color.black, label: "OLED")
                    }
                }

                // Font Size
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Cỡ chữ")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(themeManager.colors.textSecondary)
                        Spacer()
                        Text("\(Int(viewModel.fontSize)) pt")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(themeManager.colors.primary)
                    }

                    HStack(spacing: 16) {
                        Button(action: { viewModel.decreaseFontSize() }) {
                            Text("A-")
                                .font(.system(size: 15, weight: .bold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                                .background(themeManager.colors.surfaceVariant)
                                .cornerRadius(8)
                        }

                        Button(action: { viewModel.increaseFontSize() }) {
                            Text("A+")
                                .font(.system(size: 17, weight: .bold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                                .background(themeManager.colors.surfaceVariant)
                                .cornerRadius(8)
                        }
                    }
                }

                // Font Family
                VStack(alignment: .leading, spacing: 8) {
                    Text("Phông chữ")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(themeManager.colors.textSecondary)

                    HStack(spacing: 8) {
                        fontFamilyButton(family: .system, title: "Mặc định")
                        fontFamilyButton(family: .serif, title: "Serif")
                        fontFamilyButton(family: .sansSerif, title: "Sans")
                        fontFamilyButton(family: .monospace, title: "Mono")
                    }
                }

                // Line Spacing
                VStack(alignment: .leading, spacing: 8) {
                    Text("Giãn dòng")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(themeManager.colors.textSecondary)

                    HStack(spacing: 8) {
                        lineSpacingButton(multiplier: 1.3, title: "Gọn")
                        lineSpacingButton(multiplier: 1.55, title: "Vừa")
                        lineSpacingButton(multiplier: 1.85, title: "Thoáng")
                    }
                }

                Spacer()
            }
            .padding(20)
            .navigationTitle("Tùy Chỉnh Đọc")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Xong") {
                        viewModel.showSettingsSheet = false
                    }
                }
            }
        }
    }

    private func themeCircle(preset: ReaderThemePreset, bg: Color, label: String) -> some View {
        let isSelected = viewModel.themePreset == preset
        return Button(action: { viewModel.setThemePreset(preset) }) {
            VStack(spacing: 4) {
                Circle()
                    .fill(bg)
                    .frame(width: 36, height: 36)
                    .overlay(
                        Circle()
                            .stroke(isSelected ? themeManager.colors.primary : themeManager.colors.border, lineWidth: isSelected ? 2.5 : 1)
                    )
                Text(label)
                    .font(.system(size: 10))
                    .foregroundColor(isSelected ? themeManager.colors.primary : themeManager.colors.textSecondary)
            }
        }
    }

    private func fontFamilyButton(family: ReaderFontFamily, title: String) -> some View {
        let isSelected = viewModel.fontFamily == family
        return Button(action: { viewModel.setFontFamily(family) }) {
            Text(title)
                .font(.system(size: 12, weight: isSelected ? .bold : .regular))
                .foregroundColor(isSelected ? themeManager.colors.onPrimary : themeManager.colors.textPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(isSelected ? themeManager.colors.primary : themeManager.colors.surfaceVariant)
                .cornerRadius(8)
        }
    }

    private func lineSpacingButton(multiplier: CGFloat, title: String) -> some View {
        let isSelected = abs(viewModel.lineSpacingMultiplier - multiplier) < 0.1
        return Button(action: { viewModel.setLineSpacing(multiplier) }) {
            Text(title)
                .font(.system(size: 12, weight: isSelected ? .bold : .regular))
                .foregroundColor(isSelected ? themeManager.colors.onPrimary : themeManager.colors.textPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(isSelected ? themeManager.colors.primary : themeManager.colors.surfaceVariant)
                .cornerRadius(8)
        }
    }
}
