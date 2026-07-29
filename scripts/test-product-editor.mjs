import assert from 'node:assert/strict'
import { createRequire } from 'node:module'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const require = createRequire(import.meta.url)
const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
let pageDefinition

globalThis.Page = (definition) => {
  pageDefinition = definition
}

require(resolve(root, 'miniprogram/pages/merchant/product-edit/index.js'))

function normalize(optionGroups) {
  return pageDefinition.buildOptionGroups.call({ data: { optionGroups } })
}

const normalized = normalize([
  {
    name: ' 温度 ',
    required: true,
    mode: 'single',
    minSelect: '0',
    maxSelect: '8',
    options: [
      { name: ' 热 ', extraPrice: '0', enabled: true },
      { name: '少冰', extraPrice: '1.5', enabled: true }
    ]
  },
  {
    name: '加料',
    required: false,
    mode: 'multiple',
    minSelect: '2',
    maxSelect: '2',
    options: [
      { name: '珍珠', extraPrice: '1', enabled: true },
      { name: '椰果', extraPrice: '2', enabled: false }
    ]
  }
])

assert.deepEqual(normalized[0], {
  name: '温度',
  required: true,
  minSelect: 1,
  maxSelect: 1,
  sortOrder: 10,
  options: [
    { name: '热', extraPrice: 0, enabled: true, sortOrder: 10 },
    { name: '少冰', extraPrice: 1.5, enabled: true, sortOrder: 20 }
  ]
})
assert.equal(normalized[1].minSelect, 0)
assert.equal(normalized[1].maxSelect, 2)
assert.equal(normalized[1].sortOrder, 20)
assert.equal(normalized[1].options[1].enabled, false)

assert.throws(() => normalize([
  {
    name: '口味',
    required: true,
    mode: 'multiple',
    minSelect: '1',
    maxSelect: '3',
    options: [
      { name: '草莓', extraPrice: '0', enabled: true },
      { name: '香草', extraPrice: '0', enabled: true }
    ]
  }
]), /最多选择数量应为 1～2/)

assert.throws(() => normalize([
  {
    name: '甜度',
    required: true,
    mode: 'single',
    minSelect: '1',
    maxSelect: '1',
    options: [{ name: '', extraPrice: '0', enabled: true }]
  }
]), /选项的名称/)

assert.throws(() => normalize([
  {
    name: '温度',
    required: true,
    mode: 'single',
    minSelect: '1',
    maxSelect: '1',
    options: [{ name: '热', extraPrice: '0', enabled: false }]
  }
]), /至少需要 1 个可选择的选项/)

assert.throws(() => normalize([
  {
    name: '温度',
    required: true,
    mode: 'single',
    minSelect: '1',
    maxSelect: '1',
    options: [
      { name: '热', extraPrice: '0', enabled: true },
      { name: '热', extraPrice: '0', enabled: true }
    ]
  }
]), /选项“热”重复了/)

console.log('商品规格编辑器测试通过：单选、多选、可选规则、加价与排序均正确。')
