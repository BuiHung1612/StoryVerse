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
                        Text(chapter.title)
                            .font(.system(size: viewModel.fontSize + 4, weight: .bold))
                            .foregroundColor(themeManager.colors.readerTextColor)
                            .padding(.bottom, 8)

                        Text(chapter.content)
                            .font(.system(size: viewModel.fontSize))
                            .foregroundColor(themeManager.colors.readerTextColor)
                            .lineSpacing(viewModel.fontSize * 0.45)

                        // Bottom chapter navigation buttons inside scroll
                        HStack {
                            if let prev = viewModel.getPrevChapter() {
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

                            if let next = viewModel.getNextChapter() {
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
                    StoryVerseTopBar(
                        title: viewModel.currentChapter?.title ?? localizedString(AppStringKey.appName),
                        canNavigateBack: true,
                        onNavigateBack: { dismiss() }
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
                            if let prev = viewModel.getPrevChapter() {
                                self.chapterId = prev.id
                                viewModel.loadChapter(storyId: storyId, chapterId: prev.id)
                            }
                        }) {
                            Image(systemName: "backward.fill")
                                .foregroundColor(viewModel.getPrevChapter() != nil ? themeManager.colors.textPrimary : themeManager.colors.textMuted)
                        }
                        .disabled(viewModel.getPrevChapter() == nil)

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
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(themeManager.colors.surfaceVariant.opacity(0.6))
                        .cornerRadius(20)

                        Spacer()

                        // Next button
                        Button(action: {
                            if let next = viewModel.getNextChapter() {
                                self.chapterId = next.id
                                viewModel.loadChapter(storyId: storyId, chapterId: next.id)
                            }
                        }) {
                            Image(systemName: "forward.fill")
                                .foregroundColor(viewModel.getNextChapter() != nil ? themeManager.colors.textPrimary : themeManager.colors.textMuted)
                        }
                        .disabled(viewModel.getNextChapter() == nil)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(themeManager.colors.card)
                    .cornerRadius(16)
                    .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: 2)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 16)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
        .onAppear {
            viewModel.loadChapter(storyId: storyId, chapterId: chapterId)
        }
    }
}
