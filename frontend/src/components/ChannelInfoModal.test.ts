import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChannelInfoModal from '@/components/ChannelInfoModal.vue'
import { useMemberStore } from '@/stores/useMemberStore'
import { useAuthStore } from '@/stores/useAuthStore'

// ストアのモック化
vi.mock('@/stores/useMemberStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useMemberStore')>()
  return { ...actual, useMemberStore: vi.fn() }
})

vi.mock('@/stores/useAuthStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useAuthStore')>()
  return { ...actual, useAuthStore: vi.fn() }
})

describe('ChannelInfoModal.vue (テスト結果表)', () => {
  let mockAddMember: ReturnType<typeof vi.fn>
  let mockFetchMembers: ReturnType<typeof vi.fn>

  let membersData: any[]
  let authUserState: any
  let isUserAdmin: boolean

  const sampleChannel = {
    id: 10,
    name: 'general',
    createdAt: '2026-06-01T10:00:00',
  }

  const allUsersList = [
    { userId: 1, username: 'admin_user', role: 0 },
    { userId: 2, username: 'member_user1', role: 1 },
    { userId: 3, username: 'member_user2', role: 1 }, // 未参加ユーザー想定
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockAddMember = vi.fn().mockResolvedValue(undefined)
    mockFetchMembers = vi.fn().mockResolvedValue(undefined)

    // 初期状態：ユーザーID 1 と 2 が参加中
    membersData = [
      { userId: 1, username: 'admin_user', role: 0 },
      { userId: 2, username: 'member_user1', role: 1 },
    ]

    authUserState = { userId: 1, username: 'admin_user', role: 0 }
    isUserAdmin = true

    ;(useMemberStore as any).mockReturnValue({
      get members() { return membersData },
      addMember: mockAddMember,
      fetchMembers: mockFetchMembers,
    })

    ;(useAuthStore as any).mockReturnValue({
      get user() { return authUserState },
      get isAdmin() { return isUserAdmin },
    })

    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. メンバー表示 & 参加数の確認 (項番109)
  it('ストアの memberStore.members を基にした最新の人数とメンバー一覧、日時・権限が正しく表示されること', async () => {
    const wrapper = mount(ChannelInfoModal, {
      props: {
        channel: sampleChannel,
        members: [], // 旧props（ストア利用のため空でも動作すること）
        allUsers: allUsersList,
        channelId: 10,
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // チャンネル名とフォーマット済み作成日
    expect(wrapper.text()).toContain('チャンネル名 : general')
    expect(wrapper.text()).toContain('チャンネル作成日 : 2026-06-01 10:00')

    // 参加メンバー数（2名）と一覧の表示
    expect(wrapper.find('h3').text()).toContain('参加メンバー (2名)')
    const memberItems = wrapper.findAll('.member-list li')
    expect(memberItems.length).toBe(2)
    expect(memberItems[0].text()).toContain('admin_user (管理者)')
    expect(memberItems[1].text()).toContain('member_user1 (一般ユーザー)')
  })

  // 2. ユーザー追加 (サブモーダル) と一括招待の成功確認 (項番110)
  it('「+ ユーザーを追加」から未参加ユーザーを選択して決定すると、追加処理と一覧再取得が行われること', async () => {
    const wrapper = mount(ChannelInfoModal, {
      props: {
        channel: sampleChannel,
        members: [],
        allUsers: allUsersList,
        channelId: 10,
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // ユーザー追加ボタンをクリックしてサブモーダルを開く
    await wrapper.find('.btn.btn--secondary').trigger('click')
    expect(wrapper.find('.sub-modal-overlay').exists()).toBe(true)

    // 未参加ユーザー（member_user2 / userId: 3）のみが選択肢に抽出されていることを確認
    const checkboxes = wrapper.findAll('.user-checkbox-list input[type="checkbox"]')
    expect(checkboxes.length).toBe(1)

    // チェックを入れて「決定」をクリック
    await checkboxes[0].setValue(true)
    await wrapper.find('.sub-modal-actions .btn--primary').trigger('click')
    await flushPromises()

    expect(mockAddMember).toHaveBeenCalledWith(10, 3)
    expect(mockFetchMembers).toHaveBeenCalledWith(10)
    expect(window.alert).toHaveBeenCalledWith('選択したユーザーを追加しました')
    expect(wrapper.emitted('memberUpdated')).toBeTruthy()
  })

  // 3. 異常系：未選択での追加エラー確認 (項番112)
  it('ユーザー未選択の状態で「決定」を押すと、アラートで「ユーザーを選択してください」と通知されAPIが呼ばれないこと', async () => {
    const wrapper = mount(ChannelInfoModal, {
      props: {
        channel: sampleChannel,
        members: [],
        allUsers: allUsersList,
        channelId: 10,
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // サブモーダルを開く
    await wrapper.find('.btn.btn--secondary').trigger('click')

    // 何も選択せずに「決定」をクリック
    await wrapper.find('.sub-modal-actions .btn--primary').trigger('click')
    await flushPromises()

    expect(window.alert).toHaveBeenCalledWith('ユーザーを選択してください')
    expect(mockAddMember).not.toHaveBeenCalled()
  })

  // 4. チャンネル管理・操作ボタン（退出・削除）のイベント確認
  it('「チャンネルから退出する」や「チャンネルを削除する」をクリックすると対応するイベントが発火すること', async () => {
    const wrapper = mount(ChannelInfoModal, {
      props: {
        channel: sampleChannel,
        members: [],
        allUsers: allUsersList,
        channelId: 10,
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    const dangerButtons = wrapper.findAll('.btn--danger')
    // 1番目: 退出, 2番目: 削除（管理者のみ表示）
    expect(dangerButtons.length).toBe(2)

    await dangerButtons[0].trigger('click')
    expect(wrapper.emitted('leave')).toBeTruthy()

    await dangerButtons[1].trigger('click')
    expect(wrapper.emitted('delete')).toBeTruthy()
  })
})