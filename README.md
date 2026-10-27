# DSA Lab

> A hands-on laboratory for exploring data structures, algorithms, algorithmic patterns, and problem-solving techniques.
> This monorepo brings together practical implementations, structured investigations, and interactive visualizations to deepen understanding of fundamental DSA concepts.

### Goals
- Understand how data structures and algorithms work through practical implementation.
- Investigate reusable patterns for solving algorithmic problems.
- Visualize algorithm execution to make complex concepts easier to understand.
- Compare approaches and analyze time and space complexity.
- Build a structured, reusable reference for continued learning and experimentation.

### Tech Stack
(individual projects may use different tools)
- **Language:** Java 21
- **Backend:** JDK `HttpServer`, Gson 
- **Frontend:** Vanilla JavaScript, HTML, CSS, Canvas
- **Communication:** Server-Sent Events (SSE)

---

## Projects

---

### 1. DSA Patterns Visual

**Directory:** [`dsa-patterns-visual/`](./dsa-patterns-visual/README.md)

Live, interactive visualizations of classic algorithmic patterns.
A lightweight Java server built with JDK `HttpServer` and Gson (no framework) executes algorithms and streams their execution through **Server-Sent Events (SSE)**. A vanilla JavaScript canvas UI visualizes the execution with playback controls, including pause, step, and scrubbing.

---

### 2. DSA Patterns Investigation

**Directory:** [`dsa-patterns-investigation/`](./dsa-patterns-investigation/README.md)

A structured investigation of **15 essential algorithmic patterns**, implemented in Java 21.
The project explores their core principles, implementation techniques, and applications to problem-solving.

---

### 3. DSA Patterns Visual Simple

**Directory:** [`dsa-patterns-visual-simple/`](./dsa-patterns-visual-simple/README.md)

Note: this Project is just an extension of Project #2: dsa-patterns-investigation (we can see how it works)
An interactive, zero-dependency (almost) web application that visualizes 15 fundamental Data Structures & Algorithms patterns step-by-step.
Built with **Java 21** (JDK `HttpServer`), **Vanilla JavaScript**, and **HTML5 Canvas** — no Spring Boot, no React, no heavy frameworks. Just pure algorithmic visualization.

---



