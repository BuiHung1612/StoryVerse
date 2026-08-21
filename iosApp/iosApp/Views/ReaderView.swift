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

    public var body: some View {
        ZStack {
            // Main Reader Background
            themeManager.colors.readerBackground
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
                                .font(.system(size: viewModel.fontSize + 4, weight: .bold))
                                .foregroundColor(themeManager.colors.primary)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)

                            HStack(spacing: 8) {
                                if viewModel.wordCount > 0 {
                                    Text("\(viewModel.wordCount) từ")
                                        .font(.system(size: 12))
                                        .foregroundColor(themeManager.colors.textSecondary)
                                    Text("•")
                                        .font(.system(size: 12))
                                        .foregroundColor(themeManager.colors.textMuted)
                                }
                                Text("~\(viewModel.estimatedReadMinutes) phút đọc")
                                    .font(.system(size: 12))
                                    .foregroundColor(themeManager.colors.textSecondary)
                            }
                        }
                        .padding(.top, 16)
                        .padding(.bottom, 12)

                        // Paragraphs
                        if !chapter.paragraphs.isEmpty {
                            ForEach(Array(chapter.paragraphs.enumerated()), id: \.offset) { _, paragraph in
                                Text(paragraph)
                                    .font(.system(size: viewModel.fontSize))
                                    .foregroundColor(themeManager.colors.readerTextColor)
                                    .lineSpacing(viewModel.fontSize * 0.45)
                            }
                        } else {
                            Text(chapter.content)
                                .font(.system(size: viewModel.fontSize))
                                .foregroundColor(themeManager.colors.readerTextColor)
                                .lineSpacing(viewModel.fontSize * 0.45)
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
                    .padding(20)
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

                        Button(action: { viewModel.toggleTocSheet() }) {
                            Image(systemName: "list.bullet")
                                .font(.system(size: 16))
                                .foregroundColor(themeManager.colors.textPrimary)
                        }
                        .padding(.leading, 8)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(themeManager.colors.background.opacity(0.95))
                    .overlay(
                        Divider().background(themeManager.colors.border),
                        alignment: .bottom
                    )

                    Spacer()
                }
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

                        // Font size adjustment
                        HStack(spacing: 12) {
                            Button(action: { viewModel.decreaseFontSize() }) {
                                Text("A-")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                            }

                            Text("\(Int(viewModel.fontSize)) pt")
                                .font(.system(size: 12))
                                .foregroundColor(themeManager.colors.textSecondary)

                            Button(action: { viewModel.increaseFontSize() }) {
                                Text("A+")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                            }
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
    }
}
