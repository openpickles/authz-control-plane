export const helpContent = {
    policy_editor: {
        title: "Policy Editor Guide",
        content: `
**Editor Shortcuts**
- **Trigger Suggestions**: \`Ctrl\` + \`Space\`
- **Save Policy**: \`Ctrl\` + \`S\` (or \`Cmd\` + \`S\`)

**Toolbar Actions**
- **Validate**: Checks your code for syntax errors without running it.
- **Run Tests**: Opens the test panel to verify policy logic against JSON inputs.
- **Pull/Push**: Sync your policy with the configured Git repository.

**Modes**
- **Manual**: You can edit the code directly.
- **Git**: The code is **Read-Only** in the editor. You must update the file in your Git repository and pull changes here.
`
    },
    resource_types: {
        title: "Resource Types Guide",
        content: `
**What are Resource Types?**
Resource Types define the schema and structure of the data your policies will protect.

**Matching Logic**
- **Glob Patterns**: Use wildcards like \`*\` or \`**\` to match resource IDs.
- **Actions**: Define what operations (e.g., \`read\`, \`write\`) are valid for this resource.

**Integration**
Ensure your application sends resource identifiers that match these definitions when querying the Policy Engine.
`
    }
};
