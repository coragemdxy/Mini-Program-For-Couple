import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const miniRoot = join(root, 'miniprogram')

function parseJson(file) {
  try {
    return JSON.parse(readFileSync(file, 'utf8'))
  } catch (error) {
    throw new Error(`${file.replace(root + '/', '')}: JSON 无法解析：${error.message}`)
  }
}

function walk(directory) {
  return readdirSync(directory).flatMap((name) => {
    const path = join(directory, name)
    return statSync(path).isDirectory() ? walk(path) : [path]
  })
}

function validateBindings(base, label) {
  const wxml = readFileSync(base + '.wxml', 'utf8')
  const javascript = readFileSync(base + '.js', 'utf8')
  const bindings = [...wxml.matchAll(/\b(?:bind|catch)[a-zA-Z]+="([A-Za-z_$][\w$]*)"/g)]
    .map((match) => match[1])
  for (const handler of new Set(bindings)) {
    const declaration = new RegExp(`\\b${handler}\\s*\\(`)
    if (!declaration.test(javascript)) {
      throw new Error(`${label} 绑定了 ${handler}，但 JavaScript 中没有对应方法`)
    }
  }
}

const app = parseJson(join(miniRoot, 'app.json'))
const requiredGlobalFiles = ['app.js', 'app.json', 'app.wxss', 'sitemap.json']
for (const name of requiredGlobalFiles) {
  if (!existsSync(join(miniRoot, name))) {
    throw new Error(`缺少 miniprogram/${name}`)
  }
}

for (const page of app.pages) {
  const base = join(miniRoot, page)
  for (const extension of ['.js', '.json', '.wxml', '.wxss']) {
    const file = base + extension
    if (!existsSync(file)) throw new Error(`页面缺少文件：${page + extension}`)
  }
  validateBindings(base, `页面 ${page}`)
}

for (const [name, componentPath] of Object.entries(app.usingComponents || {})) {
  const base = join(miniRoot, componentPath.replace(/^\//, ''))
  for (const extension of ['.js', '.json', '.wxml', '.wxss']) {
    if (!existsSync(base + extension)) {
      throw new Error(`全局组件 ${name} 缺少文件：${componentPath + extension}`)
    }
  }
  if (parseJson(base + '.json').component !== true) {
    throw new Error(`全局组件 ${name} 的 JSON 未声明 component: true`)
  }
  validateBindings(base, `组件 ${name}`)
}

const navSource = readFileSync(join(miniRoot, 'components/role-nav/index.js'), 'utf8')
for (const match of navSource.matchAll(/\burl:\s*'\/([^']+)'/g)) {
  if (!app.pages.includes(match[1])) {
    throw new Error(`底部导航指向未注册页面：/${match[1]}`)
  }
}

const expectedNavPages = [
  ['pages/customer/home/index', 'customer', 'home'],
  ['pages/customer/cart/index', 'customer', 'cart'],
  ['pages/customer/orders/index', 'customer', 'orders'],
  ['pages/customer/profile/index', 'customer', 'profile'],
  ['pages/merchant/orders/index', 'merchant', 'orders'],
  ['pages/merchant/products/index', 'merchant', 'products'],
  ['pages/merchant/categories/index', 'merchant', 'categories'],
  ['pages/merchant/settings/index', 'merchant', 'settings']
]
for (const [page, role, active] of expectedNavPages) {
  const wxml = readFileSync(join(miniRoot, page + '.wxml'), 'utf8')
  const expected = `<role-nav role="${role}" active="${active}"`
  if (!wxml.includes(expected)) {
    throw new Error(`页面 ${page} 缺少正确的固定底部导航`)
  }
}

for (const file of walk(miniRoot)) {
  if (file.endsWith('.json')) parseJson(file)
  if (file.endsWith('.js')) {
    execFileSync(process.execPath, ['--check', file], { stdio: 'pipe' })
  }
}

const project = parseJson(join(root, 'project.config.json'))
if (project.miniprogramRoot !== 'miniprogram/') {
  throw new Error('project.config.json 的 miniprogramRoot 配置错误')
}

console.log(`小程序静态校验通过：${app.pages.length} 个页面，JSON、组件与 JavaScript 均有效。`)
