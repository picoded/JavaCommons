# Introduction

Hellos : Welcome to git book.

The following is a quick setup on how to use this repository

## Step 1: Clone this repository,

or simply copy-paste it

## Step 2: Open the command line terminal, and run the following

`npm install gitbook-cli@2.3.0 -g`

Note: Until the gitbook issue [https://github.com/GitbookIO/gitbook-cli/issues/68](https://github.com/GitbookIO/gitbook-cli/issues/68) is solved. Its best to be locking version at 2.3.0

For additional plugins, you can modify the `book.json`

Install the plugins via `gitbook install`

Refrence: https://toolchain.gitbook.com/plugins/

## Step 3: Open in Gitbook Editor / Preferred editor

For the gitbook editor, download from : [https://www.gitbook.com/editor](https://www.gitbook.com/editor)

To open a repository, under the books menu list go to : GitBook Editor &gt; open

## Step 4: To preview the gitbook

Inside the repository, note that this supports hot reloading =)

`gitbook serve`

## Extra Plugins
By default the following plugins are installed and usable.

### Blocks plugin

#### Hints

{% hint style='info' %}
**Info Header**

Info text
{% endhint %}

{% hint style='tip' %}
**Tip Header**

Tip text
{% endhint %}

{% hint style='danger' %}
**Danger Header**

Danger text
{% endhint %}

{% hint style='Working' %}
**Working Header**

Working text
{% endhint %}

See Also:
+ https://github.com/GitbookIO/plugin-hints

### Code block plugins

#### Mermaid

```mermaid
graph TD;
  A-->B;
  A-->C;
  B-->D;
  C-->D;
```

See Also: 
+ https://plugins.gitbook.com/plugin/mermaid-gb3
+ https://github.com/knsv/mermaid


#### Sequence
``` sequence
Andrew->China: Says Hello
Note right of China: China thinks\nabout it
China-->Andrew: How are you?
Andrew->>China: I am good thanks!
```

See Also: 
+ https://plugins.gitbook.com/plugin/sequence 
+ https://bramp.github.io/js-sequence-diagrams/


#### Flow
``` flow
st=>start: Start|past:>http://www.google.com[blank]
e=>end: End:>http://www.google.com
op1=>operation: My Operation|past
op2=>operation: Stuff|current
sub1=>subroutine: My Subroutine|invalid
cond=>condition: Yes
or No?|approved:>http://www.google.com
c2=>condition: Good idea|rejected
io=>inputoutput: catch something...|request

st->op1(right)->cond
cond(yes, right)->c2
cond(no)->sub1(left)->op1
c2(yes)->io->e
c2(no)->op2->e
```

See Also:
+ https://plugins.gitbook.com/plugin/new-flowchart
+ http://flowchart.js.org/

## Custom menu
See: https://toolchain.gitbook.com/pages.html

## Notes

For a good read on how to write functional specs : 
https://www.joelonsoftware.com/2000/10/02/painless-functional-specifications-part-1-why-bother/
