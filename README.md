# Serve-Down
> A web server for markdown

Serve Down is a web server that converts markdown files to HTML, and serves these over http. It was designed to host markdown source files as a website following the same directory structure as the files are written in. The objective of serve down is to host a formatted version of markdown source files as a webpage without any additional work, other than simply writing a markdown file.

Serve Down can convert markdown components to html markup, including:

- Images
- Links
- Headings
- Bulleted lists
- Horizontal Lines
- Tables
- Monospace emphasis
- Italics
- Bold fonts
- Code blocks, with syntax highlighting using HighlightJS.
- Latex typesetting using '$ ... $' for inline latex and `$$ ... $$` for latex blocks using MathJax.
- TOC generation
- CSS styling

This project came into existence following a personal need of the author, as such any further developments to the source will contribute to any additional needs the author may have. This means that although standard markdown flavours are followed to the best of the authors ability there is no guarantee that certain elements are supported other than the ones listed.  

# What's in this Repository
This repository contains:

- Serve Down's source, under `src`
- The client libraries required to convert and format markdown with the listed features, under `.client` 
- The compiled output of Serve Downs source, under `build`

# Installation

> An installation of Java compatible with JDK13 is required to run Serve Down

1. Download a release from the releases listed on Github.
2. Extract the files to a directory you wish to run the server from. Note this does not have to be where the markdown files are stored
3. Move the `.client` folder to the root directory where the markdown files are located. For example if the markdown files where located at `~/md` the `.client` folder needs to be moved so that `~/md/.client` exists.

# Usage

Run `java com.cvrskidz.servedown.Server --path {path_to_markdwon_root_directory}` 

- In the previous example you would run `java com.cvrskidz.servedown --path ~/md/`
- use `--compile` if you wish to not cache the output of converted files.
- Navigate to your clients IP address and you will be served the markdown page. 
- Include the filename such as `192.168.1.1/test`, the default file is `README.md` which replaces the traditional `index.html`. Therefore `192.168.1.1` will serve `README.html` if it exists. 
- Serve Down runs on port 80 by default.
- type `stop` to shutdown the server.

Note: Serve Down writes the converted html to disk after conversion, this is so that after the first request no conversion is required, and the server can simply serve the html file. However if you know you will change the markdown file often use `--compile` so that only the file will be converted every time.

Building upon this, if you change the markdown file and fail to see the changes reflected by the server, delete the corresponding html file. i.e. for `test.md` delete `test.html`. 

# Attribution

The distribution of Serve Down uses open source libraries provided by other authors, these include:

- MathJax for displaying latex typesetting: Apache License 2.0
- HighlightJS for highlighting syntax in code blocks: BSD 3-Clause

In accordance with each license a copy of their "License" is included in each of their respective directories under `.client/` named `LICENSE`.