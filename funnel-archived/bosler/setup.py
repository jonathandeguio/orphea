import setuptools

# with open("README.md", "r", encoding="utf-8") as fh:
#     long_description = fh.read()

setuptools.setup(
    name="boslerfunnel",
    version="0.0.1",
    author="Rakesh Malik",
    author_email="rakesh.malik@bosler.io",
    description="This is python bosler decorators.",
    long_description="long_description",
    long_description_content_type="text/markdown",
    package_dir={"": "src"},
    packages=setuptools.find_packages(where="src"),
    python_requires=">=3.6",
)
