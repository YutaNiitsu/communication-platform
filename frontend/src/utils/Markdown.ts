import MarkdownIt from "markdown-it"
import hljs from "highlight.js"
import DOMPurify from "dompurify"

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: false,

  highlight(code, language) {
    if (language && hljs.getLanguage(language)) {
      return hljs.highlight(code, {
        language
      }).value
    }

    return ""
  }
})

const defaultLinkOpen =
  md.renderer.rules.link_open ??
  ((tokens, idx, options, _env, self) =>
    self.renderToken(tokens, idx, options))

md.renderer.rules.link_open = (
  tokens,
  idx,
  options,
  env,
  self
) => {
  tokens[idx].attrSet("target", "_blank")
  tokens[idx].attrSet("rel", "noopener noreferrer")

  return defaultLinkOpen(
    tokens,
    idx,
    options,
    env,
    self
  )
}

export function renderFormattedContent(
  content: string,
  allUsers: any[],
  members: any[],
  mentionedUserIds: number[]
): string {
  if (!content) return ""

  // 1. MarkdownをHTMLへ変換
  const renderedHtml = md.render(content)

  // 2. 危険なHTMLを除去
  const sanitizedHtml = DOMPurify.sanitize(renderedHtml)

  // 3. メンション表示を付与
  const container = document.createElement("div")
  container.innerHTML = sanitizedHtml

  applyMentionFormatting(container, allUsers, members, mentionedUserIds)

  return container.innerHTML
}

function applyMentionFormatting(
    container: HTMLElement,
    allUsers: any[],
    members: any[],
    mentionedUserIds: number[]
) {
  if (mentionedUserIds.length === 0) {
    return
  }

  const walker = document.createTreeWalker(
    container,
    NodeFilter.SHOW_TEXT
  )

  const textNodes: Text[] = []

  while (walker.nextNode()) {
    const node = walker.currentNode as Text

    const parent = node.parentElement

    // コード・リンク内はメンション化しない
    if (
      parent?.closest("code") ||
      parent?.closest("pre") ||
      parent?.closest("a")
    ) {
      continue
    }

    textNodes.push(node)
  }

  for (const textNode of textNodes) {
    replaceMentionsInTextNode(
      textNode,
      mentionedUserIds,
      allUsers,
      members
    )
  }
}

function replaceMentionsInTextNode(
  textNode: Text,
  mentionedUserIds: number[],
  allUsers: any[],
  members: any[],
) {
  const text = textNode.textContent ?? ""

  const mentionRegex = /@([^\s]+)/g

  let lastIndex = 0
  let match: RegExpExecArray | null

  const fragment = document.createDocumentFragment()

  while ((match = mentionRegex.exec(text)) !== null) {
    const fullMatch = match[0]
    const username = match[1]

    const user =
      allUsers.find(
        (u: any) => u.username === username
      ) ||
      members.find(
        (m: any) => m.username === username
      )

    if (!user) {
      continue
    }

    const userId = Number(
      user.userId ?? user.id
    )

    const isMentioned =
      mentionedUserIds.some(
        (id: number) =>
          Number(id) === userId
      )

    if (!isMentioned) {
      continue
    }

    // メンションより前の通常文字
    fragment.append(
      document.createTextNode(
        text.slice(lastIndex, match.index)
      )
    )

    // メンション部分
    const span = document.createElement("span")

    span.className = "mention-pill"
    span.dataset.username = username
    span.textContent = fullMatch

    fragment.append(span)

    lastIndex =
      match.index + fullMatch.length
  }

  // メンションが1件もなかった場合
  if (lastIndex === 0) {
    return
  }

  // 最後の通常文字
  fragment.append(
    document.createTextNode(
      text.slice(lastIndex)
    )
  )

  textNode.replaceWith(fragment)
}