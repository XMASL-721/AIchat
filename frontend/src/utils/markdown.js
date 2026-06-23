/**
 * 简易 Markdown 渲染 — 把 AI 回复文本转成美化 HTML
 * 支持：加粗、代码块、行内代码、列表、换行
 */
export function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 1. 代码块 (```) — 先保护起来
  const codeBlocks = []
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
    const langLabel = lang ? `<div class="md-code-lang">${escapeHtml(lang)}</div>` : ''
    const lines = code.split('\n').map(l => escapeHtml(l)).join('<br>')
    codeBlocks.push(`<div class="md-code-block">${langLabel}<code>${lines}</code></div>`)
    return `\x00CODE${codeBlocks.length - 1}\x00`
  })

  // 2. 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')

  // 3. 加粗
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')

  // 4. 列表 — 逐行处理
  const lines = html.split('\n')
  const result = []
  let i = 0
  while (i < lines.length) {
    const line = lines[i]
    // 无序列表
    if (/^- /.test(line)) {
      const items = []
      while (i < lines.length && /^- /.test(lines[i])) {
        items.push(`<li>${lines[i].replace(/^- /, '')}</li>`)
        i++
      }
      result.push(`<ul>${items.join('')}</ul>`)
      continue
    }
    // 有序列表
    if (/^\d+\. /.test(line)) {
      const items = []
      while (i < lines.length && /^\d+\. /.test(lines[i])) {
        items.push(`<li>${lines[i].replace(/^\d+\. /, '')}</li>`)
        i++
      }
      result.push(`<ol>${items.join('')}</ol>`)
      continue
    }
    result.push(line)
    i++
  }
  html = result.join('\n')

  // 5. 段落和换行
  const parts = html.split('\n\n')
  html = parts.map(part => {
    part = part.trim()
    if (!part) return ''
    if (part.startsWith('<ul') || part.startsWith('<ol') || part.includes('\x00CODE')) return part
    return '<p>' + part.replace(/\n/g, '<br>') + '</p>'
  }).join('\n')

  // 6. 恢复代码块
  html = html.replace(/\x00CODE(\d+)\x00/g, (_, i) => codeBlocks[parseInt(i)] || '')

  return html
}

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
