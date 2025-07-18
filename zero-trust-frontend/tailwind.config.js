module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx,html}"],
  darkMode: "class",
  theme: {
    extend: {
      fontFamily: {
        sans: ['Poppins', 'Inter', 'sans-serif'],
      },
      colors: {
        global: {
          background1: "var(--global-bg-1)",
          background2: "var(--global-bg-2)",
          text1:       "var(--global-text-1)",
          text2:       "var(--global-text-2)",
          text3:       "var(--global-text-3)",
          text4:       "var(--global-text-4)",
          text5:       "var(--global-text-5)",
          text6:       "var(--global-text-6)",
        },
        sidebar: {
          background1: "var(--sidebar-bg-1)",
          text1:       "var(--sidebar-text-1)",
        },
        header: {
          background1: "var(--header-bg-1)",
          background2: "var(--header-bg-2)",
        },
        searchview: {
          background1: "var(--searchview-bg-1)",
          text1:       "var(--searchview-text-1)",
        },
        button: {
          background1: "var(--button-bg-1)",
          text1:       "var(--button-text-1)",
        },
      },
    },
  },
  plugins: [],
};