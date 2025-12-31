// Config for Monaco Editor to support Rego language
export const regoLanguageConfig = {
    id: 'rego',
    extensions: ['.rego'],
    aliases: ['Rego', 'rego'],
    mimetypes: ['application/rego'],
};

export const regoTokenConf = {
    comments: {
        lineComment: '#',
    },
    brackets: [
        ['{', '}', 'delimiter.curly'],
        ['[', ']', 'delimiter.square'],
        ['(', ')', 'delimiter.parenthesis'],
    ],
    autoClosingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '(', close: ')' },
        { open: '"', close: '"' },
        { open: '`', close: '`' },
    ],
    surroundingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '(', close: ')' },
        { open: '"', close: '"' },
        { open: '`', close: '`' },
    ],
};

export const regoLanguageDef = {
    defaultToken: '',
    tokenPostfix: '.rego',

    keywords: [
        'package', 'import', 'allow', 'deny', 'default', 'not', 'package', 'else', 'some', 'every', 'in', 'with', 'as'
    ],

    operators: [
        ':=', '==', '!=', '>', '<', '>=', '<=', '+', '-', '*', '/', '%', '&', '|'
    ],

    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

    tokenizer: {
        root: [
            // identifiers and keywords
            [/[a-z_$][\w$]*/, {
                cases: {
                    '@keywords': 'keyword',
                    '@default': 'identifier'
                }
            }],

            // whitespace
            { include: '@whitespace' },

            // delimiters and operators
            [/[{}()\[\]]/, '@brackets'],
            [/[<>](?!@symbols)/, '@brackets'],
            [/@symbols/, {
                cases: {
                    '@operators': 'operator',
                    '@default': ''
                }
            }],

            // numbers
            [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
            [/\d+/, 'number'],

            // strings
            [/"([^"\\]|\\.)*$/, 'string.invalid'],  // non-teminated string
            [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],

            // raw strings
            [/`/, { token: 'string.quote', bracket: '@open', next: '@rawstring' }],
        ],

        string: [
            [/[^\\"]+/, 'string'],
            [/@escapes/, 'string.escape'],
            [/\\./, 'string.escape.invalid'],
            [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
        ],

        rawstring: [
            [/[^`]+/, 'string'],
            [/`/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
        ],

        whitespace: [
            [/[ \t\r\n]+/, 'white'],
            [/#.*$/, 'comment'],
        ],
    },
};

export const regoSnippets = [
    {
        label: 'package',
        kind: 'Keyword',
        insertText: 'package ${1:policy}',
        documentation: 'Declare the package name for the policy.'
    },
    {
        label: 'default allow',
        kind: 'Snippet',
        insertText: 'default allow = false\n',
        documentation: 'Set the default value for the allow rule.'
    },
    {
        label: 'import data',
        kind: 'Snippet',
        insertText: 'import data.${1:namespace}\n',
        documentation: 'Import data from a namespace.'
    },
    {
        label: 'import input',
        kind: 'Snippet',
        insertText: 'import input.${1:attribute}\n',
        documentation: 'Import an attribute from the input.'
    },
    {
        label: 'print',
        kind: 'Function',
        insertText: 'print(${1:message})\n',
        documentation: 'Print a message to the console (debugging).'
    }
];
